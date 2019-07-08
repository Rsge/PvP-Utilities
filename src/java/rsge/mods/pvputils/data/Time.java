package rsge.mods.pvputils.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.DimensionManager;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;
import rsge.mods.pvputils.main.Reference;


/**
 * Time management
 * 
 * @author Rsge
 */
public class Time
{
	public static boolean tooMuchAdded;
	public static boolean playerBanned;
	private static HashMap<String, Long> playerTimes = new HashMap<String, Long>();
	private static HashMap<String, Boolean> playerOnline = new HashMap<String, Boolean>();
	private static HashMap<String, Integer> playerTimeMultiplier = new HashMap<String, Integer>();
	private static LocalDate lastDate;
	private static int spawnX;
	private static int spawnY;
	private static int spawnZ;
	private static HashMap<String, Boolean> playerInSpawn = new HashMap<String, Boolean>();

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize time data <br>
	 * Defining spawnpoint & creating/reading file for limited time
	 * 
	 * @throws IOException
	 */
	public static void init() throws IOException
	{
		// Finding the spawnpoint
		ChunkCoordinates spawn = DimensionManager.getWorld(0).getSpawnPoint();
		spawnX = spawn.posX;
		spawnY = spawn.posY;
		spawnZ = spawn.posZ;
		Logger.info("Spawnpoint at " + spawnX + ", " + spawnY + ", " + spawnZ);

		// Creating needed file or ...
		Reference.timeData = new File(Reference.dataDir, "times.dat");
		if (!Reference.timeData.exists())
		{
			Reference.timeData.createNewFile();
			Logger.info("times.dat created");

			lastDate = LocalDate.now();
			return;
		}

		// ... reading out the file
		try (BufferedReader br = Files.newBufferedReader(Reference.timeData.toPath());)
		{
			lastDate = LocalDate.parse(br.readLine());
			int entries = Integer.parseInt(br.readLine());

			for (int i = 0; i < entries; i++)
			{
				String uuid = br.readLine();

				long time;
				try
				{
					time = Long.parseLong(br.readLine());
				}
				catch (NumberFormatException ex)
				{
					time = Config.startTime;

					// Log to Console
					Logger.error("ERROR Trying to get " + uuid + "'s time. Resetting to default time");

					// Log to Cmdlog-File
					try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND))
					{
						DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
						String log = "[" + LocalDateTime.now().format(dtf) + "] [" + uuid + "] [Time reset to default because of file error]\n";

						bw.write(log);
					}
					catch (Exception exc)
					{
						Logger.error("ERROR Trying to log this reset");
					}
				}

				int mult;
				try
				{
					mult = Integer.parseInt(br.readLine());

					if (mult < 0 || mult > 100)
					{
						mult = 100;
						logMultiplierError(uuid);
					}
				}
				catch (NumberFormatException ex)
				{
					mult = 100;
					logMultiplierError(uuid);
				}

				if (Config.addedTime != 0 && !lastDate.equals(LocalDate.now()))
				{
					double multiplier = ((double) mult) / 100;
					long addedTime = (long) (Config.addedTime * multiplier);
					time += addedTime;

					Logger.info("Added " + addedTime + " seconds to " + uuid);
					if (Config.debugLogging)
						Logger.info("Mult: " + mult + "; Multiplier: " + multiplier);
				}

				playerTimes.put(uuid, time);
				playerTimeMultiplier.put(uuid, mult);
				playerOnline.put(uuid, false);
				playerInSpawn.put(uuid, false);

				if (Config.debugLogging)
					Logger.info("File: " + uuid + " has " + (time / 60) + " minutes left");
			}
			br.close();
		}
		catch (NullPointerException ex)
		{
			lastDate = LocalDate.now();

			if (Config.debugLogging)
				Logger.info("times.dat-File empty! This is not an Error!");

		}
	}

	/**
	 * Save playtime data
	 */
	public static void save()
	{
		try (BufferedWriter bw = Files.newBufferedWriter(Reference.timeData.toPath()))
		{
			bw.write(LocalDate.now().toString());
			bw.newLine();
			bw.write(Integer.toString(playerTimes.size()));
			bw.newLine();
			for (Entry<String, Long> entry : playerTimes.entrySet())
			{
				String uuid = entry.getKey();
				String time = Long.toString(entry.getValue());
				String multiplier = Integer.toString(playerTimeMultiplier.get(entry.getKey()));

				bw.write(uuid);
				bw.newLine();
				bw.write(time);
				bw.newLine();
				bw.write(multiplier);
				bw.newLine();

				if (Config.debugLogging)
					Logger.info("Map: " + uuid + " has " + (Long.parseLong(time) / 60) + " minutes left with a time modifier of " + multiplier);
			}

			bw.flush();
			bw.close();

			if (Config.debugLogging)
			{
				BufferedReader br = Files.newBufferedReader(Reference.timeData.toPath());
				br.readLine();
				int entries = Integer.parseInt(br.readLine());
				for (int i = 0; i < entries; i++)
				{
					String uuid = br.readLine();
					long time = Long.parseLong(br.readLine());
					if (Config.debugLogging)
						Logger.info("File: " + uuid + " has " + time / 60 + " minutes left");
					br.readLine();
				}
				br.close();
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Save data and clear for new init
	 */
	public static void stop()
	{
		save();
		playerTimes.clear();
		playerTimeMultiplier.clear();
	}

	/**
	 * Log error in time multiplier of player
	 * 
	 * @param uuid of player
	 */
	private static void logMultiplierError(String uuid)
	{
		// Log to Console
		Logger.error("ERROR Trying to process " + uuid + "'s multiplier. Resetting to default multiplier");

		// Log to Cmdlog-File
		try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND))
		{
			DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
			String log = "[" + LocalDateTime.now().format(dtf) + "] [" + uuid + "] [Multiplier reset to default because of error]\n";

			bw.write(log);
		}
		catch (Exception exc)
		{
			Logger.error("ERROR Trying to log this reset");
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize player with default time values
	 * 
	 * @param p Player to initialize
	 */
	public static void initPlayer(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();
		String uuid = u.toString();

		if (!playerTimes.containsKey(uuid))
		{
			long time = Config.startTime;
			playerTimes.put(uuid, time);
			Logger.info("Initialized Player " + uuid + " with " + Config.startTime / 60 + " minutes and 100 percent time-get-modifier");
		}

		if (!playerTimeMultiplier.containsKey(uuid))
			playerTimeMultiplier.put(uuid, 100);

		if (!playerOnline.containsKey(uuid))
			playerOnline.put(uuid, false);
	}

	/**
	 * Start time for player
	 * 
	 * @param p Player
	 */
	public static void startTime(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();
		String uuid = u.toString();

		if (!MinecraftServer.getServer().isSinglePlayer() && playerTimes.get(uuid) == 0)
			outOfTime(p);
		else
			playerOnline.put(uuid, true);
	}

	/**
	 * Stop time for player
	 * 
	 * @param p Player
	 */
	public static void stopTime(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();
		String uuid = u.toString();

		playerOnline.put(uuid, false);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Manage time per second
	 */
	public static void second()
	{
		boolean newDay = !lastDate.equals(LocalDate.now());

		for (Entry<String, Long> entry : playerTimes.entrySet())
		{
			long t = entry.getValue();
			boolean on;

			try
			{
				on = playerOnline.get(entry.getKey());
			}
			catch (NullPointerException ex)
			{
				on = true;
			}

			// Give extra time each day
			if (Config.addedTime != 0 && newDay)
			{
				double multiplier = ((double) playerTimeMultiplier.get(entry.getKey())) / 100;
				long addedTime = (long) (Config.addedTime * multiplier);
				t += addedTime;
				playerTimes.put(entry.getKey(), t);

				Logger.info("Added " + addedTime + " seconds to " + entry.getKey());
				if (Config.debugLogging)
					Logger.info("Mult: " + playerTimeMultiplier.get(entry.getKey()) + "; Multiplier: " + multiplier);
			}

			if (on)
			{
				// Get the player entity from the player's UUID
				UUID uuid = UUID.fromString(entry.getKey());
				@SuppressWarnings("unchecked")
				List<EntityPlayer> ps = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

				for (EntityPlayer p : ps)
				{
					UUID uuid2 = p.getGameProfile().getId();
					if (uuid.equals(uuid2))
					{
						if (Config.stopInSpawn)
						{
							int playerX = p.getPlayerCoordinates().posX;
							int playerY = p.getPlayerCoordinates().posY;
							int playerZ = p.getPlayerCoordinates().posZ;
							if (Config.excessiveLogging)
								Logger.info(uuid2.toString() + ": " + playerX + ", " + playerY + ", " + playerZ);

							int comparedX = playerX - spawnX;
							boolean inRangeX = comparedX >= -Config.stopInSpawnRadius && comparedX <= Config.stopInSpawnRadius;
							int comparedY = playerY - spawnY;
							boolean inRangeY = comparedY >= -Config.stopInSpawnRadius && comparedY <= Config.stopInSpawnRadius;
							int comparedZ = playerZ - spawnZ;
							boolean inRangeZ = comparedZ >= -Config.stopInSpawnRadius && comparedZ <= Config.stopInSpawnRadius;

							if (Config.excessiveLogging)
								Logger.info(entry.getKey() + " ComparedX = " + comparedX + " ComparedY = " + comparedY + " ComparedZ = " + comparedZ);

							if (!inRangeX || !inRangeY || !inRangeZ)
							{
								try
								{
									if (!playerInSpawn.get(uuid2.toString()))
									{
										playerInSpawn.put(uuid2.toString(), true);
										p.addChatMessage(new ChatComponentText("You left the spawnzone, your time started again.")
												.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
									}

									passTime(entry, p, t);

									if (Config.excessiveLogging)
										Logger.info(uuid2.toString() + "'s second passed");
								}
								catch (NullPointerException ex)
								{
									playerInSpawn.put(uuid2.toString(), false);
								}
							}
							else
							{
								try
								{
									if (playerInSpawn.get(uuid2.toString()))
									{
										playerInSpawn.put(uuid2.toString(), false);
										p.addChatMessage(new ChatComponentText("You entered the spawnzone, your time stopped.")
												.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
									}

									if (Config.excessiveLogging)
										Logger.info(uuid2.toString() + "'s second didn't pass");
								}
								catch (NullPointerException ex)
								{}
							}
						}
						else
							passTime(entry, p, t);
					}
				}
			}
		}
		lastDate = LocalDate.now();
	}

	/**
	 * Remove time from player
	 * 
	 * @param entry UUID of player and his/her/it's time in seconds
	 * @param p     Player
	 * @param t     Time in seconds
	 */
	private static void passTime(Entry<String, Long> entry, EntityPlayer p, long t)
	{
		// Remind the player of his remaining time
		if (t == 1800 || t == 600 || t == 60 || t == 10 || t <= 5)
		{
			if (t <= 0)
			{
				t = 0;
				outOfTime((EntityPlayerMP) p);
			}
			else
				chatTime(p);
		}

		t -= 1;

		playerTimes.put(entry.getKey(), t);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Chat time of player to player
	 * 
	 * @param p Player
	 */
	public static void chatTime(EntityPlayer p)
	{
		chatTimeTo(p, p, true);
	}

	/**
	 * Chat time of player to receiver
	 * 
	 * @param p        Player who's time to look at
	 * @param receiver Player who send command
	 * @param self     If receiver is looking at own time
	 */
	public static void chatTimeTo(EntityPlayer p, EntityPlayer receiver, boolean self)
	{
		UUID uuid = p.getGameProfile().getId();
		long t = Time.getTime(uuid);
		long h = t / 3600;
		byte min = (byte) ((t % 3600) / 60);
		byte sec = (byte) (t % 60);
		double compare = (double) t / (double) (Config.addedTime);

		IChatComponent msgStart;
		IChatComponent msgHour;
		IChatComponent msgMin;
		IChatComponent msgSec;
		IChatComponent msgTimes;
		IChatComponent msgEnd;

		IChatComponent msgTime;

		// Who is addressed?
		if (self)
			msgStart = new ChatComponentText("You currently have ");
		else
			msgStart = new ChatComponentText(p.getCommandSenderName() + " currently has ");

		// Use correct Grammar
		if (t % 3600 == 0)
		{
			if (h == 1)
				msgHour = new ChatComponentText(h + " hour ");
			else
				msgHour = new ChatComponentText(h + " hours ");
		}
		else if (t % 60 == 0 && h > 0)
		{
			if (h == 1)
				msgHour = new ChatComponentText(h + " hour and ");
			else
				msgHour = new ChatComponentText(h + " hours and ");
		}
		else if (h == 1)
			msgHour = new ChatComponentText(h + " hour, ");
		else
			msgHour = new ChatComponentText(h + " hours, ");

		if (t % 60 == 0)
			msgMin = new ChatComponentText(min + " minutes ");
		else if (min == 1)
			msgMin = new ChatComponentText(min + " minute and ");
		else
			msgMin = new ChatComponentText(min + " minutes and ");

		if (sec == 1)
			msgSec = new ChatComponentText(sec + " second ");
		else
			msgSec = new ChatComponentText(sec + " seconds ");

		// Put the time together
		if (t % 3600 == 0)
			msgTimes = msgHour;
		else if (t % 60 == 0 && h > 0)
			msgTimes = msgHour.appendSibling(msgMin);
		else if (h > 0)
			msgTimes = msgHour.appendSibling(msgMin).appendSibling(msgSec);
		else if (min > 0)
			msgTimes = msgMin.appendSibling(msgSec);
		else
			msgTimes = msgSec;

		// I put it here for order's sake
		msgEnd = new ChatComponentText("left.");

		// Show the time in different colors depending on their ratio to the max time
		if (compare <= 0.1)
			msgTimes.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
		else if (compare <= 0.25)
			msgTimes.getChatStyle().setColor(EnumChatFormatting.RED);
		else if (compare <= 0.5)
			msgTimes.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		else if (compare <= 0.75)
			msgTimes.getChatStyle().setColor(EnumChatFormatting.GREEN);
		else
			msgTimes.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);

		// Put the message together
		msgTime = msgStart.appendSibling(msgTimes).appendSibling(msgEnd);
		msgTime.setChatStyle(new ChatStyle().setBold(true));

		// And send it to the player
		receiver.addChatMessage(msgTime);

		if (Config.debugLogging)
			Logger.info("Time: Compare = " + compare);
	}

	/**
	 * @param  uuid of player
	 * @return      time of player with UUID
	 */
	public static long getTime(UUID uuid)
	{
		return (long) playerTimes.get(uuid.toString());
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Reset time of player to start value
	 * 
	 * @param p Player
	 */
	public static void resetTime(EntityPlayerMP p)
	{
		Time.setTime(p, Config.startTime);
	}

	/**
	 * Set time of player to given value
	 * 
	 * @param p Player
	 * @param t Time in seconds
	 */
	public static void setTime(EntityPlayerMP p, long t)
	{
		UUID uuid = p.getGameProfile().getId();

		if (t <= Config.maxTime)
			playerTimes.put(uuid.toString(), t);
		else
		{
			playerTimes.put(uuid.toString(), Config.maxTime);
			Time.tooMuchAdded = true;
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Add standard amount of time to player
	 * 
	 * @param p Player
	 */
	public static void addTime(EntityPlayerMP p)
	{
		addTime(p, Config.addedTime);
	}

	/**
	 * Add given amount of time to player
	 * 
	 * @param p Player
	 * @param t Time in seconds
	 */
	public static void addTime(EntityPlayerMP p, long t)
	{
		UUID uuid = p.getGameProfile().getId();
		long time = playerTimes.get(uuid.toString());

		if (time + t < Config.maxTime)
		{
			time += t;
		}
		else
		{
			time = Config.maxTime;
			tooMuchAdded = true;
		}
		playerTimes.put(uuid.toString(), time);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Remove standard amount of time from player
	 * 
	 * @param p Player
	 */
	public static void removeTime(EntityPlayerMP p)
	{
		removeTime(p, Config.addedTime);
	}

	/**
	 * Remove given amount of time from player
	 * 
	 * @param p Player
	 * @param t Time in seconds
	 */
	public static void removeTime(EntityPlayerMP p, long t)
	{
		UUID uuid = p.getGameProfile().getId();
		long time = playerTimes.get(uuid.toString());

		if (time - t > 0)
		{
			time -= t;
		}
		else
		{
			time = 0;
			outOfTime(p);
			playerBanned = true;
		}
		playerTimes.put(uuid.toString(), time);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Kick player from server because of time
	 * 
	 * @param p Player
	 */
	private static void outOfTime(EntityPlayerMP p)
	{
		String msg = "You're out of Time! Log in again tomorrow.";
		p.playerNetServerHandler.kickPlayerFromServer(msg);

		/*
		 * MinecraftServer mcs = MinecraftServer.getServer(); if (mcs.isSinglePlayer() && p.getCommandSenderName().equals(mcs.getServerOwner())) { } else {
		 * UserListBansEntry banEntry = new UserListBansEntry(p.getGameProfile(), (Date) null, Reference.NAME, (Date) null, banReason);
		 * mcs.getConfigurationManager().func_152608_h().func_152687_a(banEntry); p.playerNetServerHandler.kickPlayerFromServer(msg); }
		 */
	}
}
