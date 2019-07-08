package rsge.mods.pvputils.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;
import rsge.mods.pvputils.main.Reference;


/**
 * Lives management
 * 
 * @author Rsge
 */
public class Lives
{
	public static boolean tooManyAdded;
	public static boolean playerBanned;
	public static boolean worldDelete;
	private static HashMap<String, Byte> playerLives = new HashMap<String, Byte>();

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize lives data
	 * 
	 * @throws IOException
	 */
	public static void init() throws IOException
	{
		Reference.lifeData = new File(Reference.dataDir, "lives.dat");
		if (!Reference.lifeData.exists())
		{
			// Create the life-data and exit
			Reference.lifeData.createNewFile();
			Logger.info("lives.dat created");
			return;
		}

		BufferedReader br = Files.newBufferedReader(Reference.lifeData.toPath());
		int entries = Integer.parseInt(br.readLine());
		for (int i = 0; i < entries; i++)
		{
			String uuid = br.readLine();
			byte lives;
			try
			{
				lives = Byte.parseByte(br.readLine());

				playerLives.put(uuid, lives);

				if (Config.debugLogging)
					Logger.info("File: " + uuid + " has " + lives + " lives left");
			}
			catch (NumberFormatException ex)
			{
				lives = Config.startLives;

				// Log to Console
				Logger.error("ERROR Trying to get " + uuid + "'s lives. Resetting to starting lives");
				// Log to Cmdlog-File
				try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND))
				{
					DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
					String log = "[" + LocalDateTime.now().format(dtf) + "] [" + uuid + "] [Lives reset to default because of file error]\n";

					bw.write(log);
				}
				catch (Exception exc)
				{
					Logger.error("ERROR Trying to log this reset");
				}
			}
		}
		br.close();
	}

	/**
	 * Save lives data
	 */
	public static void save()
	{
		try (BufferedWriter bw = Files.newBufferedWriter(Reference.lifeData.toPath()))
		{
			bw.write(Integer.toString(playerLives.size()));
			bw.newLine();
			for (Entry<String, Byte> entry : playerLives.entrySet())
			{
				String uuid = entry.getKey();
				String lives = Byte.toString(entry.getValue());

				bw.write(uuid);
				bw.newLine();
				bw.write(lives);
				bw.newLine();

				if (Config.debugLogging)
					Logger.info("Map: " + uuid + " has " + lives + " lives left");
			}

			bw.flush();
			bw.close();

			if (Config.debugLogging)
			{
				BufferedReader br = Files.newBufferedReader(Reference.lifeData.toPath());
				int entries = Integer.parseInt(br.readLine());
				for (int i = 0; i < entries; i++)
				{
					String uuid = br.readLine();
					byte lives = Byte.parseByte(br.readLine());
					if (Config.debugLogging)
						Logger.info("File: " + uuid + " has " + lives + " lives left");
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
		playerLives.clear();
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize player lives with starting values
	 * 
	 * @param p Player
	 */
	public static void initPlayer(EntityPlayerMP p)
	{
		UUID uuid = p.getGameProfile().getId();
		if (!playerLives.containsKey(uuid.toString()))
		{
			playerLives.put(uuid.toString(), Config.startLives);
			Logger.info("Initialized Player " + uuid.toString() + " with " + Config.startLives + " lives");
		}

	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Chat lives of player to player
	 * 
	 * @param p Player
	 */
	public static void chatLives(EntityPlayer p)
	{
		chatLivesTo(p, p, true);
	}

	/**
	 * Chat lives of player to receiver
	 * 
	 * @param p        Player who's lives to look at
	 * @param receiver Player who send command
	 * @param self     If receiver is looking at own lives
	 */
	public static void chatLivesTo(EntityPlayer p, EntityPlayer receiver, boolean self)
	{
		UUID uuid = p.getGameProfile().getId();
		byte l = Lives.getLives(uuid);
		double compare = (double) l / (double) Config.maxLives;
		IChatComponent msgLives = new ChatComponentText("" + l).setChatStyle(new ChatStyle().setBold(true));
		IChatComponent msgSelf = new ChatComponentText("You currently have ").setChatStyle(new ChatStyle().setBold(true));
		IChatComponent msgOther = new ChatComponentText(p.getCommandSenderName() + " currently has ").setChatStyle(new ChatStyle().setBold(true));
		IChatComponent msgEnd;

		// Use correct grammar
		if (l == 1)
			msgEnd = new ChatComponentText(" life left.");
		else
			msgEnd = new ChatComponentText(" lives left.");

		// Show the number of lives in different colors depending on their ratio to the max lives
		if (compare <= 0.1)
			msgLives.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
		else if (compare <= 0.25)
			msgLives.getChatStyle().setColor(EnumChatFormatting.RED);
		else if (compare <= 0.5)
			msgLives.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		else if (compare <= 0.75)
			msgLives.getChatStyle().setColor(EnumChatFormatting.GREEN);
		else if (compare <= 1)
			msgLives.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
		else
			msgLives.getChatStyle().setColor(EnumChatFormatting.GOLD);

		if (self)
			receiver.addChatMessage(msgSelf.appendSibling(msgLives).appendSibling(msgEnd));
		else
			receiver.addChatMessage(msgOther.appendSibling(msgLives).appendSibling(msgEnd));

		if (Config.debugLogging)
			Logger.info("Lives: Compare = " + compare);
	}

	/**
	 * @param  uuid of player
	 * @return      lives of player with UUID
	 */
	public static byte getLives(UUID uuid)
	{
		return playerLives.get(uuid.toString());

	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Reset lives of player to start value
	 * 
	 * @param p Player
	 */
	public static void resetLives(EntityPlayerMP p)
	{
		setLives(p, Config.startLives);
	}

	/**
	 * Set lives of player to given value
	 * 
	 * @param p Player
	 * @param l Amount of lives
	 */
	public static void setLives(EntityPlayerMP p, int l)
	{
		UUID uuid = p.getGameProfile().getId();

		if (l <= Config.maxLives)
			playerLives.put(uuid.toString(), (byte) l);
		else
		{
			playerLives.put(uuid.toString(), Config.maxLives);
			Lives.tooManyAdded = true;
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Add standard amount of lives to player
	 * 
	 * @param p Player
	 */
	public static void addLife(EntityPlayerMP p)
	{
		addLives(p, 1);

	}

	/**
	 * Add given amount of time to player
	 * 
	 * @param p Player
	 * @param l Amount of lives
	 */
	public static void addLives(EntityPlayerMP p, int l)
	{
		UUID uuid = p.getGameProfile().getId();
		byte lives = playerLives.get(uuid.toString());
		if (lives + l < Config.maxLives)
		{
			lives += l;
		}
		else
		{
			lives = Config.maxLives;
			tooManyAdded = true;
		}
		playerLives.replace(uuid.toString(), lives);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Handle lives on player death
	 * 
	 * @param p   Player
	 * @param src Damage source that killed player
	 */
	public static void death(EntityPlayerMP p, DamageSource src)
	{
		boolean wasPlayer = src.getDamageType().equals("player") || src.getEntity() instanceof EntityPlayer;
		boolean wasMonster = src.getDamageType().equals("mob") || src.getEntity() instanceof EntityLiving;

		if (Config.livesTakenBy == 1 && wasPlayer)
		{
			removeLife(p);
		}
		else if (Config.livesTakenBy == 2 && (wasPlayer || wasMonster))
		{
			removeLife(p);
		}
		else if (Config.livesTakenBy == 3)
		{
			removeLife(p);
		}
	}

	/**
	 * Remove 1 life from player
	 * 
	 * @param p Player
	 */
	public static void removeLife(EntityPlayerMP p)
	{
		removeLives(p, 1);
	}

	/**
	 * Remove given amount of lives from player
	 * 
	 * @param p Player
	 * @param l Amount of lives
	 */
	public static void removeLives(EntityPlayerMP p, int l)
	{
		UUID uuid = p.getGameProfile().getId();
		byte lives = playerLives.get(uuid.toString());
		if (lives - l > 0)
		{
			lives -= l;
		}
		else
		{
			lives = 0;
			outOfLives(p);
			playerBanned = true;
		}
		playerLives.replace(uuid.toString(), lives);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Handle what happens when player has no lives left on death
	 * 
	 * @param p Player
	 */
	private static void outOfLives(EntityPlayerMP p)
	{
		// HQM had some problems with TConstruct, so I'll clear the player's inventory to prevent them, too O:)
		p.inventory.clearInventory(null, -1);

		MinecraftServer mcs = MinecraftServer.getServer();
		String deathMessage = "You're out of Lives! Sorry, the battle is over for you :(";
		String banReason = "Out of lives in battle";

		if (mcs.isSinglePlayer() && p.getCommandSenderName().equals(mcs.getServerOwner()))
		{
			worldDelete = true;
			((EntityPlayerMP) p).playerNetServerHandler.kickPlayerFromServer(deathMessage);
			mcs.deleteWorldAndStopServer();
		}
		else
		{
			UserListBansEntry banEntry = new UserListBansEntry(p.getGameProfile(), (Date) null, Reference.NAME, (Date) null, banReason);
			mcs.getConfigurationManager().func_152608_h().func_152687_a(banEntry);
			p.playerNetServerHandler.kickPlayerFromServer(deathMessage);
		}
	}
}
