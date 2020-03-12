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

import com.mojang.authlib.GameProfile;

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
	private static HashMap<UUID, Byte> playerLives = new HashMap<UUID, Byte>();

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

		try (BufferedReader br = Files.newBufferedReader(Reference.lifeData.toPath());)
		{
			int entries = Integer.parseInt(br.readLine());
			for (int i = 0; i < entries; i++)
			{
				String uuid = br.readLine();
				UUID u = null;
				String lives = br.readLine();
				byte l;
				try
				{
					u = UUID.fromString(uuid);
					l = Byte.parseByte(lives);

					playerLives.put(u, l);

					if (Config.debugLogging)
						Logger.info(u + " has " + lives + " lives left");
				}
				catch (NumberFormatException ex)
				{
					l = Config.startLives;

					String clog = "ERROR Trying to get " + uuid + "'s lives (Found " + lives + "). Resetting to starting lives.";
					String flog = "[Found " + lives + " instead of number. Reset to default.]";
					logError(uuid, clog, flog);
				}
				catch (IllegalArgumentException ex)
				{
					String clog = "ERROR: " + uuid + " is not a valid UUID. It had " + lives + " associated with it. Deleting now!";
					String flog = "[Not a valid UUID. It had " + lives + " associated with it. Deleted entry.]";
					logError(uuid, clog, flog);
				}
			}
			br.close();
		}
		catch (NullPointerException ex)
		{
			if (Config.debugLogging)
				Logger.info("lives.dat-File empty! This is not an Error!");
		}
	}

	/**
	 * Log error in life reading process
	 * 
	 * @param uuid UUID of errored player
	 * @param clog Console log string
	 * @param flog File log string
	 */
	private static void logError(String uuid, String clog, String flog)
	{
		// Log to console
		Logger.error(clog);
		// Log to Cmdlog-File
		try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND))
		{
			DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
			flog = "[" + LocalDateTime.now().format(dtf) + "] [" + uuid + "] " + flog + "\n";
			bw.write(flog);
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
			Logger.error("Lives saving failed: " + ex.getLocalizedMessage());
			return false;
		}
		finally
		{
			playerLives.clear();
		}
		return true;
	}

	/**
	 * Save lives data
	 * 
	 * @throws IOException
	 */
	public static void save() throws IOException
	{
		BufferedWriter bw = Files.newBufferedWriter(Reference.lifeData.toPath());

		bw.write(Integer.toString(playerLives.size()));
		bw.newLine();
		for (Entry<UUID, Byte> entry : playerLives.entrySet())
		{
			String uuid = entry.getKey().toString();
			String lives = Byte.toString(entry.getValue());

			bw.write(uuid);
			bw.newLine();
			bw.write(lives);
			bw.newLine();
		}

		bw.flush();
		bw.close();
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Initialize player lives with starting values
	 * 
	 * @param p Player
	 */
	public static void initPlayer(EntityPlayerMP p)
	{
		UUID u = p.getGameProfile().getId();
		if (!playerLives.containsKey(u))
		{
			playerLives.put(u, Config.startLives);
			Logger.info("Initialized Player " + u.toString() + " with " + Config.startLives + " lives");
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
		chatLivesTo(p, p);
	}

	/**
	 * Chat lives of player to receiver
	 * 
	 * @param p   Player who's lives to look at
	 * @param rec Player who sent command and receives info
	 */
	public static void chatLivesTo(EntityPlayer p, EntityPlayer rec)
	{
		UUID u = p.getGameProfile().getId();
		byte l = getLives(u);
		IChatComponent msgStart;
		IChatComponent msgLives;
		IChatComponent msgEnd;

		// Who is addressed?
		if (p.equals(rec))
			msgStart = new ChatComponentText("You currently have ");
		else
			msgStart = new ChatComponentText(p.getCommandSenderName() + " currently has ");

		// Format lives in appropriate color
		msgLives = formatLives(l);

		// Use correct grammar
		if (l == 1)
			msgEnd = new ChatComponentText(" life left.");
		else
			msgEnd = new ChatComponentText(" lives left.");

		// Put the message together
		IChatComponent msgFinal = msgStart.appendSibling(msgLives).appendSibling(msgEnd);
		msgFinal.setChatStyle(new ChatStyle().setBold(true));

		// And send it to the player
		rec.addChatMessage(msgFinal);
	}

	/**
	 * Format live amount with appropriate colors
	 * 
	 * @param  l Amount of lives as byte
	 * @return   Chat-component of lives in appropriate color
	 */
	public static IChatComponent formatLives(byte l)
	{
		double compare = (double) l / (double) Config.maxLives;
		IChatComponent msgLives = new ChatComponentText(Long.toString(l));
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
		if (Config.debugLogging)
			Logger.info("Lives: Compare = " + compare);
		return msgLives;
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Handle lives on player death
	 * 
	 * @param p   EntityPlayerMP of player
	 * @param src Damage source that killed player
	 */
	public static void death(EntityPlayerMP p, DamageSource src)
	{
		boolean wasPlayer = src.getDamageType().equals("player") || src.getEntity() instanceof EntityPlayer;
		boolean wasMonster = src.getDamageType().equals("mob") || src.getEntity() instanceof EntityLiving;

		if (Config.livesTakenBy == 1 && wasPlayer)
		{
			removeLives(p, 1);
		}
		else if (Config.livesTakenBy == 2 && (wasMonster || wasPlayer))
		{
			removeLives(p, 1);
		}
		else if (Config.livesTakenBy == 3)
		{
			removeLives(p, 1);
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Add given amount of lives to all players
	 * 
	 * @param l Amount of lives
	 */
	public static void addLivesToAll(int l)
	{
		for (UUID u : playerLives.keySet())
		{
			addLives(u, l);
		}
	}

	/**
	 * Add given amount of lives to player
	 * 
	 * @param p EntityPlayerMP of player
	 * @param l Amount of lives
	 */
	public static void addLives(EntityPlayerMP p, int l)
	{
		UUID u = p.getGameProfile().getId();
		addLives(u, l);
	}

	/**
	 * Add given amount of lives to player with UUID
	 * 
	 * @param u UUID of player
	 * @param l Amount of lives
	 */
	public static void addLives(UUID u, int l)
	{
		byte lives = getLives(u);
		setLives(u, lives + l);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Remove given amount of lives from all players
	 * 
	 * @param l Amount of lives
	 */
	public static void removeLivesFromAll(int l)
	{
		for (UUID u : playerLives.keySet())
		{
			removeLives(u, l);
		}
	}

	/**
	 * Remove given amount of lives from player
	 * 
	 * @param p EntityPlayerMP of player
	 * @param l Amount of lives
	 */
	public static void removeLives(EntityPlayerMP p, int l)
	{
		UUID u = p.getGameProfile().getId();
		byte lives = getLives(u);
		setLives(p, lives - l);
	}

	/**
	 * Remove given amount of lives from player with UUID
	 * 
	 * @param u UUID of player
	 * @param l Amount of lives
	 */
	public static void removeLives(UUID u, int l)
	{
		byte lives = getLives(u);
		setLives(u, lives - l);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Reset lives of all players to starting value
	 */
	public static void resetAllLives()
	{
		setAllLives(Config.startLives);
	}

	/**
	 * Reset lives of player to starting value
	 * 
	 * @param p EntityPlayerMP of player
	 */
	public static void resetLives(EntityPlayerMP p)
	{
		setLives(p, Config.startLives);
	}

	/**
	 * Reset lives of player with UUID to starting value
	 * 
	 * @param u UUID of player
	 */
	public static void resetLives(UUID u)
	{
		setLives(u, Config.startLives);
	}
	
	/* ————————————————————————————————————————————————————— */

	/**
	 * Set lives of all players to given value
	 * 
	 * @param l Amount of lives
	 */
	public static void setAllLives(int l)
	{
		for (UUID u : playerLives.keySet())
		{
			setLives(u, l);
		}
	}

	/**
	 * Set lives of player to given value
	 * 
	 * @param p EntityPlayerMP of player
	 * @param l Amount of lives
	 */
	public static void setLives(EntityPlayerMP p, int l)
	{
		UUID u = p.getGameProfile().getId();
		if (l > 0)
		{
			setLives(u, l);
		}
		else
		{
			playerLives.replace(u, (byte) 0);
			outOfLives(p);
			playerBanned = true;
		}
		
		if (Config.scoreboardEnabled)
			ScoreBoard.updatePlayer(p);
	}

	/**
	 * Set lives of player with UUID to given value
	 * 
	 * @param u UUID of player
	 * @param l Amount of lives
	 */
	public static void setLives(UUID u, int l)
	{
		if (l <= Config.maxLives && l > 0)
			playerLives.replace(u, (byte) l);
		else if (l > Config.maxLives)
		{
			playerLives.replace(u, Config.maxLives);
			Lives.tooManyAdded = true;
		}
		else
		{
			playerLives.replace(u, (byte) 0);
			outOfLives(u);
			playerBanned = true;
		}
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * @param  u of player
	 * @return   lives of player with UUID
	 */
	public static byte getLives(UUID u)
	{
		return playerLives.get(u);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Handle what happens when player with UUID has no lives left
	 * 
	 * @param p UUID of player
	 */
	private static void outOfLives(UUID u)
	{
		GameProfile g = new GameProfile(u, null);
		MinecraftServer mcs = MinecraftServer.getServer();
		EntityPlayerMP p = mcs.getConfigurationManager().createPlayerForUser(g);
		outOfLives(p);
	}

	/**
	 * Handle what happens when player has no lives left
	 * 
	 * @param p EntityPlayerMP of player
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
