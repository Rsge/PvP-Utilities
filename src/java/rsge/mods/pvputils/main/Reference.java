package rsge.mods.pvputils.main;

import java.io.File;


/**
 * Collection of useful variables
 * 
 * @author Rsge
 */
public class Reference
{
	/** Mod ID */
	public static final String MODID = "pvputils";
	/** Mod name */
	public static final String NAME = "PvP Utilities";
	/** Mod version */
	public static final String VERSION = "0.1.0";
	/** Mod dependencies */
	public static final String DEPENDENCIES = "required-after:Forge@[10.13.4.1448,)";
	/** Accepted Minecraft versions */
	public static final String MCVERSIONS = "[1.7, 1.7.10]";
	/** Accepted client mod versions */
	public static final String REMOTEVERSIONS = "*";
	/** Common proxy */
	public static final String COMMON_PROXY = "rsge.mods." + MODID + ".proxies.CommonProxy";
	/** Client proxy */
	public static final String CLIENT_PROXY = "rsge.mods." + MODID + ".proxies.ClientProxy";

	/** Path of config */
	public static String configPath;
	/** Config file */
	public static File configFile;
	/** Data directory */
	public static File dataDir;
	/** Life data file */
	public static File lifeData;
	/** Time data file */
	public static File timeData;
	/** Logged commands file */
	public static File loggedCmds;
}
