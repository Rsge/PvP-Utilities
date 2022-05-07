package rsge.mods.pvputils.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;
import rsge.mods.pvputils.commands.CmdHandler;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.data.ScoreBoard;
import rsge.mods.pvputils.data.Time;
import rsge.mods.pvputils.listeners.CommandEventListener;
import rsge.mods.pvputils.listeners.PlayerAttackInteractEventListener;
import rsge.mods.pvputils.listeners.PlayerDeathEventListener;
import rsge.mods.pvputils.listeners.XpPickupListener;
import rsge.mods.pvputils.proxies.CommonProxy;


/**
 * Main class <br>
 * Serverside-Mod <br>
 * <br>
 * This is a Mod, Forge =P <br>
 * 
 * @author Rsge
 */
@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES, acceptedMinecraftVersions = Reference.MCVERSIONS, acceptableRemoteVersions = Reference.REMOTEVERSIONS)
public class PvPUtils {
	/**
	 * PvPUtils Instance
	 */
	@Instance(value = Reference.MODID)
	public static PvPUtils instance;

	/* ————————————————————————————————————————————————————— */

	/**
	 * Common proxy <br>
	 * (No client proxy needed)
	 */
	@SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
	public static CommonProxy proxy;

	/* ————————————————————————————————————————————————————— */

	/**
	 * Config initilization
	 * 
	 * @param e Pre-Initialization event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		Reference.configPath = e.getModConfigurationDirectory().getAbsolutePath();
		Reference.configFile = new File(Reference.configPath + File.separator + Reference.MODID + ".cfg");
		Config.init(Reference.configFile);
	}

	/**
	 * Registering modules
	 * 
	 * @param e Initialization event
	 */
	@EventHandler
	public void init(FMLInitializationEvent e) {
		FMLCommonHandler.instance().bus().register(instance);
		if (Config.cmdlogEnabled)
			new CommandEventListener();
		if (Config.macroDisable)
			new PlayerAttackInteractEventListener();
		if (Config.xpLockEnabled)
			new XpPickupListener();
		if (Config.livesEnabled)
			new PlayerDeathEventListener();
	}

	/**
	 * Logging jff <br>
	 * Don't ask, it's just for the sake of it =P <br>
	 * It's German by the way, in case you were wondering
	 * 
	 * @param e Post-Initialization event
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		Logger.info("Kleine Katzen leben laenger mit Calgon, Nyan, Nyan!");
		if (Config.debugLogging)
			Logger.info("Debug-logging enabled");
		if (Config.excessiveLogging)
			Logger.info("Excessive debug-logging enabled");
		if (Config.constantExcessiveLogging)
			Logger.info("Constant excessive debug-logging enabled");
	}

	/**
	 * Registering commands
	 * 
	 * @param e Server starting event
	 */
	@EventHandler
	public void serverLoad(FMLServerStartingEvent e) {
		e.registerServerCommand(CmdHandler.instance);
	}

	/**
	 * Data initialization
	 * 
	 * @param e Server started event
	 */
	@EventHandler
	public void serverLoaded(FMLServerStartedEvent e) {
		try{
			if (Config.livesEnabled || Config.timeEnabled || Config.cmdlogEnabled){
				Reference.dataDir = new File(DimensionManager.getCurrentSaveRootDirectory(), Reference.MODID);
				if (!Reference.dataDir.exists())
					Reference.dataDir.mkdirs();

				if (Config.livesEnabled){
					Lives.init();

					if (Config.scoreboardEnabled)
						ScoreBoard.init();
				}

				if (Config.timeEnabled)
					Time.init();

				if (Config.cmdlogEnabled){
					Reference.loggedCmds = new File(Reference.dataDir, "LoggedCommands.txt");
					if (!Reference.loggedCmds.exists())
						Reference.loggedCmds.createNewFile();
				}
			}
		}
		catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Stopping time & saving everything
	 * 
	 * @param e Server stopped event
	 */
	@EventHandler
	public void serverStop(FMLServerStoppedEvent e) {
		// Because the owner (sometimes?) doesn't seem to log out of a SP-world
		MinecraftServer mcs = MinecraftServer.getServer();
		if (mcs.isSinglePlayer()){
			@SuppressWarnings("rawtypes")
			List players = mcs.getConfigurationManager().playerEntityList;
			if (!players.isEmpty()){
				EntityPlayerMP owner = (EntityPlayerMP) players.get(0);

				if (Config.timeEnabled)
					Time.stopTime(owner);

				if (Config.debugLogging)
					Logger.info("Player '" + owner.getCommandSenderName() + "' with ID " + owner.getGameProfile().getId().toString() + " logged out");
			}

			if (!Lives.worldDelete){
				// Safe data
				if (Config.livesEnabled)
					Lives.stop();
				if (Config.timeEnabled)
					Time.stop();
			}
			else
				Lives.worldDelete = false;
		}

		// Who gets this reference? :D
		Logger.info("I don't hate you.");
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Event Subscriptions <br>
	 * For whatever reason THESE cannot be outsourced to another file... <br>
	 * The OTHERS however HAVE to be outsourced... <br>
	 * I don't understand it, but never touch a running system, am I right? ; )
	 */

	/**
	 * Initializing playerbound data: <br>
	 * Lives & time
	 * 
	 * @param e Player logged in event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoggedInEvent e) {
		if (Config.debugLogging)
			Logger.info("Player '" + e.player.getCommandSenderName() + "' with ID '" + e.player.getGameProfile().getId().toString() + "' logged in");

		if (e.player instanceof EntityPlayerMP){
			EntityPlayerMP p = (EntityPlayerMP) e.player;

			// Lives
			if (Config.livesEnabled){
				Lives.initPlayer(p);

				if (!Config.noLifeChat)
					Lives.chatLives(p);

				if (Config.scoreboardEnabled)
					ScoreBoard.updatePlayer(p);
			}

			// Time
			if (Config.timeEnabled){
				Time.initPlayer(p);

				if (!Config.noTimeChat)
					Time.chatTime(p);

				Time.startTime(p);
			}
		}
	}

	/**
	 * Saving playerbound data: <br>
	 * Lives & time
	 * 
	 * @param e Player logged out event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerLogout(PlayerLoggedOutEvent e) {
		if (Config.debugLogging)
			Logger.info("Player '" + e.player.getCommandSenderName() + "' with ID " + e.player.getGameProfile().getId().toString() + " logged out");

		if (e.player instanceof EntityPlayerMP){
			EntityPlayerMP p = (EntityPlayerMP) e.player;
			if (Config.livesEnabled){
				try{
					Lives.save();
				}
				catch (IOException ex){
					Logger.error("Lives saving failed: " + ex.getLocalizedMessage());
				}
			}

			if (Config.timeEnabled){
				if (!MinecraftServer.getServer().isSinglePlayer())
					Time.stopTime(p);
				try{
					Time.save();
				}
				catch (IOException ex){
					Logger.error("Time saving failed: " + ex.getLocalizedMessage());
				}
			}
			if (Config.macroDisable){
				PlayerAttackInteractEventListener.logout(e.player);
			}
		}
	}

	private int i = 0;

	/**
	 * Time passing
	 * 
	 * @param e Server tick event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onServerTick(ServerTickEvent e) {
		i += 1;

		if (i == 40){
			if (Config.constantExcessiveLogging)
				Logger.info("Second passed");

			if (Config.timeEnabled)
				Time.second();

			i = 0;
		}
	}

	private static HashMap<EntityPlayer, Boolean[]> msgRec = new HashMap<EntityPlayer, Boolean[]>();

	/**
	 * XP capping
	 * 
	 * @param e Player tick event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerTick(PlayerTickEvent e) {
		if (Config.xpLockEnabled){
			if (!msgRec.containsKey(e.player))
				msgRec.put(e.player, new Boolean[] {false, false});

			if (!msgRec.get(e.player)[0] && e.player.experienceLevel == Config.xpLockLevel - 1){
				ChatComponentText msg = new ChatComponentText("You are one level away from max!");
				msg.getChatStyle().setColor(EnumChatFormatting.RED);
				e.player.addChatMessage(msg);
				msgRec.put(e.player, new Boolean[] {true, msgRec.get(e.player)[1]});
			}
			else if (!msgRec.get(e.player)[1] && e.player.experienceLevel == Config.xpLockLevel){
				ChatComponentText msg = new ChatComponentText("You reached max level! XP-orbs won't be collected! Any further XP will be deleted!");
				msg.getChatStyle().setColor(EnumChatFormatting.RED);
				e.player.addChatMessage(msg);
				msgRec.put(e.player, new Boolean[] {msgRec.get(e.player)[0], true});
			}
			else if (msgRec.get(e.player)[0] && e.player.experienceLevel < Config.xpLockLevel - 1){
				msgRec.put(e.player, new Boolean[] {false, false});
			}

			if (e.player.experienceLevel == Config.xpLockLevel && e.player.experience > 0)
				e.player.experience = 0;

			else if (e.player.experienceLevel > Config.xpLockLevel)
				e.player.experienceLevel = Config.xpLockLevel;
		}
	}
}
