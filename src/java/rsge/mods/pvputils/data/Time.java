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
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

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
	public static boolean playerOutOfTime;
	private static HashMap<UUID, Long> playerTimes = new HashMap<UUID, Long>();
	private static HashMap<UUID, Boolean> playerOnline = new HashMap<UUID, Boolean>();
	private static HashMap<UUID, Float> playerTimeMultiplier = new HashMap<UUID, Float>();
	private static HashMap<UUID, Boolean> playerInSpawn = new HashMap<UUID, Boolean>();
	private static LocalDate lastDate;
	private static ChunkCoordinates spawn;

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize time data <br>
	 * Defining spawn point & creating/reading file for time
	 * 
	 * @throws IOException
	 */
	public static void init() throws IOException
	{
		// Finding the spawn point
		spawn = DimensionManager.getWorld(0).getSpawnPoint();
		Logger.info("Spawnpoint at " + spawn.posX + ", " + spawn.posY + ", " + spawn.posZ);

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
				UUID u = null;
				String time = br.readLine();
				long t = 0;
				String mult = br.readLine();
				float m = 0.0f;

				try
				{
					u = UUID.fromString(uuid);
					t = Long.parseLong(time);
				}

				catch (NumberFormatException ex)
				{
					t = Config.startTime;
					String clog = "ERROR Trying to get " + uuid + "'s time (Found " + time + "). Resetting to default time";
					String flog = "[Found " + time + " instead of number. Reset to default.]";
					logError(uuid, clog, flog);
				}
				catch (IllegalArgumentException ex)
				{
					String clog = "ERROR: " + uuid + " is not a valid UUID. It had " + time + " associated with it. Deleting now!";
					String flog = "[Not a valid UUID. It had " + time + " associated with it. Deleted entry.]";
					logError(uuid, clog, flog);
				}
				try
				{
					m = Float.parseFloat(mult);

					if (m < 0.0f || m > 50.0f)
					{
						m = 1.0f;
						throw new NumberFormatException();
					}
					if (m > 10.0f)
						Logger.warn("Time-multiplier for " + uuid + " is very high (" + mult + ") - possible error?");
				}
				catch (NumberFormatException ex)
				{
					m = 1.0f;
					String clog = "ERROR Trying to process multiplier. Resetting to default.";
					String flog = "[Multiplier reset to default because of error]";
					logError(uuid, clog, flog);
				}

				playerTimeMultiplier.put(u, m);
				playerOnline.put(u, false);
				playerInSpawn.put(u, false);

				if (!newDay(u, t))
					playerTimes.put(u, t);

				if (Config.debugLogging)
					Logger.info(uuid + " has " + (t / 60) + " minutes left with a time modifier of " + m);
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
	 * Log error in time reading process
	 * 
	 * @param uuid UUID of errored player
	 * @param clog Console log string
	 * @param flog File log string
	 */
	private static void logError(String uuid, String clog, String flog)
	{
		// Log to Console
		Logger.error(clog);

		// Log to Cmdlog-File
		try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND))
		{
			DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
			String log = "[" + LocalDateTime.now().format(dtf) + "] [" + uuid + "] " + flog + "\n";

			bw.write(log);
		}
		catch (Exception exc)
		{
			Logger.error("ERROR Trying to log this reset");
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Save data and clear for new init
	 */
	public static boolean stop()
	{
		try
		{
			save();
		}
		catch (IOException ex)
		{
			Logger.error("Time saving failed: " + ex.getLocalizedMessage());
			return false;
		}
		finally
		{
			playerTimes.clear();
			playerTimeMultiplier.clear();
		}
		return true;
	}

	/**
	 * Save time data
	 * 
	 * @throws IOException
	 */
	public static void save() throws IOException
	{
		BufferedWriter bw = Files.newBufferedWriter(Reference.timeData.toPath());

		bw.write(LocalDate.now().toString());
		bw.newLine();
		bw.write(Integer.toString(playerTimes.size()));
		bw.newLine();
		for (Entry<UUID, Long> entry : playerTimes.entrySet())
		{
			UUID u = entry.getKey();
			Long t = entry.getValue();
			Float m = playerTimeMultiplier.get(u);

			bw.write(u.toString());
			bw.newLine();
			bw.write(t.toString());
			bw.newLine();
			bw.write(m.toString());
			bw.newLine();

			if (Config.debugLogging)
				Logger.info(u.toString() + " has " + (t / 60) + " minutes left with a time modifier of " + m);
		}

		bw.flush();
		bw.close();
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize player with default time values if necessary
	 * 
	 * @param p Player to initialize
	 */
	public static void initPlayer(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();
		float m = 1.0f;

		if (!playerTimes.containsKey(u))
		{
			long time = Config.startTime;
			playerTimes.put(u, time);
			Logger.info("Initialized Player " + u.toString() + " with " + Config.startTime / 60 + " minutes and " + m + " time-get-modifier");
		}

		if (!playerTimeMultiplier.containsKey(u))
			playerTimeMultiplier.put(u, m);

		if (!playerOnline.containsKey(u))
			playerOnline.put(u, false);
	}

	/**
	 * Start time for player
	 * 
	 * @param p Player
	 */
	public static void startTime(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();

		if (!MinecraftServer.getServer().isSinglePlayer() && playerTimes.get(u) == 0)
			outOfTime(p);
		else
			playerOnline.put(u, true);
	}

	/**
	 * Stop time for player
	 * 
	 * @param p Player
	 */
	public static void stopTime(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();

		playerOnline.put(u, false);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Manage time per second
	 */
	public static void second()
	{
		// For each player...
		for (Entry<UUID, Long> entry : playerTimes.entrySet())
		{
			UUID u = entry.getKey();
			long t = entry.getValue();
			boolean on = playerOnline.get(u);

			// ... if player is online
			if (on)
			{
				// Get the player entity from the player's UUID
				// This is probably a lot more efficient than the old version...
				GameProfile g = new GameProfile(u, null);
				MinecraftServer mcs = MinecraftServer.getServer();
				EntityPlayerMP p = mcs.getConfigurationManager().createPlayerForUser(g);

				// Stop time if player is in spawn area
				if (Config.stopInSpawn)
				{
					// Squared distance makes all this much easier and the radius a circle
					double d = Math.sqrt(p.getPlayerCoordinates().getDistanceSquared(spawn.posX, spawn.posY, spawn.posZ));
					if (Config.constantExcessiveLogging)
						Logger.info("Distance to spawn of " + u.toString() + ": " + d + " blocks");

					if (d > Config.stopInSpawnRadius)
					{
						try
						{
							if (!playerInSpawn.get(u))
							{
								playerInSpawn.put(u, true);
								p.addChatMessage(new ChatComponentText("You left the spawnzone, your time started again.")
										.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
							}

							passTime(u, p, t);

							if (Config.constantExcessiveLogging)
								Logger.info(u.toString() + "'s second passed");
						}
						catch (NullPointerException ex)
						{
							playerInSpawn.put(u, false);
						}
					}
					else
					{
						try
						{
							if (playerInSpawn.get(u))
							{
								playerInSpawn.put(u, false);
								p.addChatMessage(new ChatComponentText("You entered the spawnzone, your time stopped.")
										.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
							}

							if (Config.constantExcessiveLogging)
								Logger.info(u.toString() + "'s second didn't pass");
						}
						catch (NullPointerException ex)
						{}
					}
				}
				else
					passTime(u, p, t);
			}
		}
		lastDate = LocalDate.now();
	}

	/**
	 * Remove time from player
	 * 
	 * @param u UUID of player
	 * @param p EntityPlayer of player
	 * @param t Time in seconds
	 */
	private static void passTime(UUID u, EntityPlayer p, long t)
	{
		// Remind the player of his remaining time
		if (t == 1800 || t == 600 || t == 60 || t == 10 || t <= 5)
		{
			// If player has no time left
			if (t <= 0)
			{
				t = 0;
				outOfTime((EntityPlayerMP) p);
				return;
			}
			else
				chatTime(p);
		}
		t -= 1;
		playerTimes.replace(u, t);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Chat time of player to player
	 * 
	 * @param p Player
	 */
	public static void chatTime(EntityPlayer p)
	{
		chatTimeTo(p, p);
	}

	/**
	 * Chat time of player to receiver
	 * 
	 * @param p   Player who's time to look at
	 * @param rec Player who sent command and receives info
	 */
	public static void chatTimeTo(EntityPlayer p, EntityPlayer rec)
	{
		UUID u = p.getGameProfile().getId();
		long t = Time.getTime(u);
		IChatComponent msgStart;
		IChatComponent msgTime;
		IChatComponent msgEnd;

		// Who is addressed?
		if (p.equals(rec))
			msgStart = new ChatComponentText("You currently have ");
		else
			msgStart = new ChatComponentText(p.getCommandSenderName() + " currently has ");

		// Format time in readable format and appropriate color
		msgTime = formatTime(t);

		// I put it here for order's sake
		msgEnd = new ChatComponentText("left.");

		// Put the message together
		IChatComponent msgFinal = msgStart.appendSibling(msgTime).appendSibling(msgEnd);
		msgFinal.setChatStyle(new ChatStyle().setBold(true));

		// And send it to the player
		rec.addChatMessage(msgFinal);
	}

	public static IChatComponent formatTime(long t)
	{
		IChatComponent msgHour;
		IChatComponent msgMin;
		IChatComponent msgSec;
		IChatComponent msgTime;

		long h = t / 3600;
		byte m = (byte) ((t % 3600) / 60);
		byte s = (byte) (t % 60);
		double compare = (double) t / (double) (Config.addedTime);

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
			msgMin = new ChatComponentText(m + " minutes ");
		else if (m == 1)
			msgMin = new ChatComponentText(m + " minute and ");
		else
			msgMin = new ChatComponentText(m + " minutes and ");

		if (s == 1)
			msgSec = new ChatComponentText(s + " second ");
		else
			msgSec = new ChatComponentText(s + " seconds ");

		// Put the time together
		if (t % 3600 == 0)
			msgTime = msgHour;
		else if (t % 60 == 0 && h > 0)
			msgTime = msgHour.appendSibling(msgMin);
		else if (h > 0)
			msgTime = msgHour.appendSibling(msgMin).appendSibling(msgSec);
		else if (m > 0)
			msgTime = msgMin.appendSibling(msgSec);
		else
			msgTime = msgSec;

		// Show the time in different colors depending on their ratio to the max time
		if (compare <= 0.1)
			msgTime.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
		else if (compare <= 0.25)
			msgTime.getChatStyle().setColor(EnumChatFormatting.RED);
		else if (compare <= 0.5)
			msgTime.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		else if (compare <= 0.75)
			msgTime.getChatStyle().setColor(EnumChatFormatting.GREEN);
		else
			msgTime.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);

		if (Config.debugLogging)
			Logger.info("Time: Compare = " + compare);

		return msgTime;
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Add given amount of time to all players
	 * 
	 * @param t Amount of time in seconds
	 */
	public static void addTimeToAll(long t)
	{
		for (UUID u : playerTimes.keySet())
		{
			addTime(u, t);
		}
	}

	/**
	 * Add given amount of time to player
	 * 
	 * @param p EntityPlayerMP of player
	 * @param t Amount of time in seconds
	 */
	public static void addTime(EntityPlayerMP p, long t)
	{
		UUID u = p.getGameProfile().getId();
		addTime(u, t);
	}

	/**
	 * Add given amount of time to player with UUID
	 * 
	 * @param u UUID of player
	 * @param t Amount of time in seconds
	 */
	public static void addTime(UUID u, long t)
	{
		long time = getTime(u);
		setTime(u, time + t);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Remove given amount of time from all players
	 * 
	 * @param t Amount of time in seconds
	 */
	public static void removeTimeFromAll(long t)
	{
		for (UUID u : playerTimes.keySet())
		{
			removeTime(u, t);
		}
	}

	/**
	 * Remove given amount of time from player
	 * 
	 * @param p EntityPlayerMP of player
	 * @param t Amount of time in seconds
	 */
	public static void removeTime(EntityPlayerMP p, long t)
	{
		UUID u = p.getGameProfile().getId();
		long time = getTime(u);
		setTime(p, time - t);
	}

	/**
	 * Remove given amount of time from player with UUID
	 * 
	 * @param u UUID of player
	 * @param t Amount of time in seconds
	 */
	public static void removeTime(UUID u, long t)
	{
		long time = getTime(u);
		setTime(u, time - t);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Reset time of all players to starting value
	 */
	public static void resetAllTime()
	{
		setAllTime(Config.startTime);
	}

	/**
	 * Reset time of player to starting value
	 * 
	 * @param p EntityPlayerMP of player
	 */
	public static void resetTime(EntityPlayerMP p)
	{
		setTime(p, Config.startTime);
	}
	
	/**
	 * Reset time of player with UUID to starting value
	 * 
	 * @param u UUID of player
	 */
	public static void resetTime(UUID u)
	{
		setTime(u, Config.startTime);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Set time of all players to given value
	 * 
	 * @param t Amount of time in seconds
	 */
	public static void setAllTime(long t)
	{
		for (UUID u : playerTimes.keySet())
		{
			setTime(u, t);
		}
	}

	/**
	 * Set time of player to given value
	 * 
	 * @param p EntityPlayerMP of player
	 * @param t Amount of time in seconds
	 */
	public static void setTime(EntityPlayerMP p, long t)
	{
		UUID u = p.getGameProfile().getId();
		if (t > 0)
		{
			setTime(u, t);
		}
		else
		{
			playerTimes.replace(u, (long) 0);
			outOfTime(p);
			playerOutOfTime = true;
		}
	}

	/**
	 * Set time of player with UUID to given value
	 * 
	 * @param u UUID of player
	 * @param t Amount of time in seconds
	 */
	public static void setTime(UUID u, long t)
	{
		if (t <= Config.maxTime && t > 0)
			playerTimes.replace(u, t);
		else if (t > Config.maxTime)
		{
			playerTimes.replace(u, Config.maxTime);
			Time.tooMuchAdded = true;
		}
		else
		{
			playerTimes.replace(u, (long) 0);
			playerOutOfTime = true;
		}
	}
	
	/* ————————————————————————————————————————————————————— */
	
	/**
	 * Set time multiplier of all players to given value
	 * 
	 * @param m Multiplier as float
	 */
	public static void setAllTimeMultipliers(Float m)
	{
		for (UUID u : playerTimeMultiplier.keySet())
		{
			setTimeMultiplier(u, m);
		}
	}
	
	/**
	 * Set time multiplier of player to given value
	 * 
	 * @param p EntityPlayerMP of player
	 * @param m Multiplier as float
	 */
	public static void setTimeMultiplier(EntityPlayerMP p, Float m)
	{
		UUID u = p.getGameProfile().getId();
		setTimeMultiplier(u, m);
	}
	
	/**
	 * Set time multiplier of player with UUID to given value
	 * 
	 * @param u UUID of player
	 * @param m Multiplier as float
	 */
	public static void setTimeMultiplier(UUID u, Float m)
	{
		playerTimeMultiplier.replace(u, m);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Get time of player
	 * 
	 * @param  UUID of player
	 * @return      Amount of time of player with UUID in seconds
	 */
	public static long getTime(UUID u)
	{
		return (long) playerTimes.get(u);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Adds new time each day until max time is reached
	 * 
	 * @param  t    Amount of time in seconds
	 * @param  uuid UUID of player
	 * @return      true, if time was added due to a new day; false else
	 */
	private static boolean newDay(UUID u, long t)
	{
		if (Config.addedTime != 0 && !lastDate.equals(LocalDate.now()) && t < Config.maxTime)
		{
			float m = playerTimeMultiplier.get(u);
			long addedTime = (long) (Config.addedTime * m);
			if (t + addedTime > Config.maxTime)
				t = Config.maxTime;
			else
				t += addedTime;
			playerTimes.put(u, t);

			if (Config.debugLogging)
				Logger.info("New day: Added " + addedTime + " seconds to " + u.toString());

			return true;
		}
		return false;
	}

	/**
	 * Kick player from server because of time
	 * 
	 * @param p EntityPlayerMP of player
	 */
	private static void outOfTime(EntityPlayerMP p)
	{
		String msg = "You're out of Time! Log in again tomorrow.";
		p.playerNetServerHandler.kickPlayerFromServer(msg);

		/*
		 * Tempban-functionality tries - not important enough | MinecraftServer mcs = MinecraftServer.getServer(); if
		 * (mcs.isSinglePlayer() && p.getCommandSenderName().equals(mcs.getServerOwner())) { } else { UserListBansEntry
		 * banEntry = new UserListBansEntry(p.getGameProfile(), (Date) null, Reference.NAME, (Date) null, banReason);
		 * mcs.getConfigurationManager().func_152608_h().func_152687_a(banEntry);
		 * p.playerNetServerHandler.kickPlayerFromServer(msg); }
		 */
	}
}
