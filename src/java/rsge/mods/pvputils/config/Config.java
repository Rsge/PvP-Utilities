package rsge.mods.pvputils.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import rsge.mods.pvputils.main.Logger;


/**
 * Config file management
 * 
 * @author Rsge
 */
public class Config {
	private static final String CAT_MODULES_KEY = "1_Modules";
	private static final String CAT_MODULES_COMMENT = "Options to enable and disable parts of the mod.";

	private static final String CMDLOG_ENABLED_KEY = "1 Command logging";
	private static final boolean CMDLOG_ENABLED_DEFAULT = true;
	private static final String CMDLOG_ENABLED_COMMENT = "Log ALL the commands (The important ones, anyway...).";
	public static boolean cmdlogEnabled;

	private static final String MACRO_DISABLE_KEY = "2 Deny macros";
	private static final boolean MACRO_DISABLE_DEFAULT = false;
	private static final String MACRO_DISABLE_COMMENT = "(Still very buggy...) Stop players from using overly fast autoclicker-macros.";
	public static boolean macroDisable;

	private static final String XPLOCK_DISABLE_KEY = "3 Implement XP-Lock";
	private static final boolean XPLOCK_DISABLE_DEFAULT = true;
	private static final String XPLOCK_DISABLE_COMMENT = "Stop  players from having too many levels at once (Good against Last Stand).";
	public static boolean xpLockEnabled;

	private static final String LIVES_ENABLED_KEY = "4 Lives enabled";
	private static final boolean LIVES_ENABLED_DEFAULT = true;
	private static final String lives_ENABLED_COMMENT = "Limit the number of lives a player has.";
	public static boolean livesEnabled;

	private static final String TIME_ENABLED_KEY = "5 Playtime enabled";
	private static final boolean TIME_ENABLED_DEFAULT = true;
	private static final String TIME_ENABLED_COMMENT = "Limit the amount of playtime a player has.";
	public static boolean timeEnabled;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_COMMAND_LOG_KEY = "2_Command_logging";
	private static final String CAT_COMMAND_LOG_COMMENT = "Options for commandlogging.\nLog file can be found in [world-save-folder]/pvputils/LoggedCmds.txt";

	private static final String CMDLOG_WHERE_KEY = "1 Log to";
	private static final int CMDLOG_WHERE_DEFAULT = 1;
	private static final String CMDLOG_WHERE_COMMENT = "If command logging is enabled, log to: 1 = console & file, 2 = console only, 3 = file only.";
	public static int cmdlogWhere;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_MACROS_KEY = "3_Macros";
	private static final String CAT_MACROS_COMMENT = "Options to configure what counts as macro and what should be done about their users.\nMacro in this case means an autoclicker with superhuman clicking speed";

	private static final String MACRO_TRESHOLD_KEY = "1 Macro treshold";
	private static final int MACRO_TRESHOLD_DEFAULT = 50;
	private static final String MACRO_TRESHOLD_COMMENT = "Minimum time in ms between interactions before action is classified as macro.";
	public static int macroTreshold;

	private static final String MACRO_KICKER_KEY = "2 Kick macro users";
	private static final boolean MACRO_KICKER_DEFAULT = false;
	private static final String MACRO_KICKER_COMMENT = "Kick players using very fast macros.";
	public static boolean macroKicker;

	private static final String MACRO_KICKER_TRESHOLD_KEY = "3 Kick treshold";
	private static final int MACRO_KICKER_TRESHOLD_DEFAULT = 10;
	private static final String MACRO_KICKER_TRESHOLD_COMMENT = "Number of times a player has to use macros to be kicked.";
	public static int macroKickerTreshold;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_XP_KEY = "4_EXP";
	private static final String CAT_XP_COMMENT = "Options to configure an xp-lock.\nUseful for modpacks containing Last Stand (OpenBlocks) or similar possibilities.";

	private static final String XPLOCK_LEVEL_KEY = "1 Highest level";
	private static final int XPLOCK_LEVEL_DEFAULT = 100;
	private static final String XPLOCK_LEVEL_COMMENT = "If XP-Lock enabled, this is the highest achievable level.";
	public static int xpLockLevel;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_LIVES = "5_Lives";
	private static final String CAT_LIVES_COMMENT = "Options to configure the limited lives feature.\nlives.dat file can be found in [world-save-folder]/pvputils.\nIt contains the number of entries and a list of uuids with their respective number of lives.";

	private static final String LIVES_KEY = "1 Default lives";
	private static final int LIVES_DEFAULT = 10;
	private static final String LIVES_COMMENT = "How many lives should one have in the beginning.";
	public static byte startLives;

	private static final String MAXLIVES_KEY = "2 Max lives";
	private static final int MAXLIVES_DEFAULT = 10;
	private static final String MAXLIVES_COMMENT = "How many lives one can have at maximum.";
	public static byte maxLives;

	private static final String LIVES_TAKEN_KEY = "3 Lives taken by";
	private static final int LIVES_TAKEN_DEFAULT = 1;
	private static final String LIVES_TAKEN_COMMENT = "You lose a life when you are killed by: 1=Players only, 2=Players and monsters, 3=Everything.";
	public static int livesTakenBy;

	private static final String NO_LIFE_CHAT_KEY = "4 Disable life info";
	private static final boolean NO_LIFE_CHAT_DEFAULT = false;
	private static final String NO_LIFE_CHAT_COMMENT = "Disable the chat message showing your current lives when you log in.";
	public static boolean noLifeChat;

	private static final String SCOREBOARD_ENABLED_KEY = "5 Scoreboard enabled";
	private static final boolean SCOREBOARD_ENABLED_DEFAULT = true;
	private static final String SCOREBOARD_ENABLED_COMMENT = "Create a scoreboard-objective for the amount of lives a player has left.";
	public static boolean scoreboardEnabled;

	private static final String SCOREBOARD_TYPE_KEY = "6 Scoreboard type";
	private static final int SCOREBOARD_TYPE_DEFAULT = 1;
	private static final String SCOREBOARD_TYPE_COMMENT = "The type of scoreboard created: 1=List, 2=Sidebar, 3=Below name.";
	public static int scoreboardType;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_TIME = "6_Time";
	private static final String CAT_TIME_COMMENT = "Options to configure limited playtime feature.\nUseful for 24/7 servers still wanting more balance between casual players and players with too much free time ; )\ntimes.dat can be found in [world-save-folder]/pvputils It contains the last saved date, the number of entries and\na list of uuids with their respective time in s and the time gaining modifier.";

	private static final String ADD_TIME_KEY = "1 Daily added playtime";
	private static final int ADD_TIME_DEFAULT = 120;
	private static final String ADD_TIME_COMMENT = "How much time in minutes one should get every day. Set this to 0 to disable it.";
	public static long addedTime;

	private static final String START_TIME_KEY = "2 Playtime at start";
	private static final int START_TIME_DEFAULT = 120;
	private static final String START_TIME_COMMENT = "How much time in minutes one should have on first login.";
	public static long startTime;

	private static final String MAXTIME_KEY = "3 Max playtime";
	private static final int MAXTIME_DEFAULT = 1440;
	private static final String MAXTIME_COMMENT = "How much time in minutes one can have at maximum.";
	public static long maxTime;

	private static final String NO_TIME_CHAT_KEY = "4 Disable time info";
	private static final boolean NO_TIME_CHAT_DEFAULT = false;
	private static final String NO_TIME_CHAT_COMMENT = "Disable the chat message showing your current time when you log in / time runs out.";
	public static boolean noTimeChat;

	private static final String STOP_IN_SPAWN_KEY = "5 Stop time in spawn";
	private static final boolean STOP_IN_SPAWN_DEFAULT = true;
	private static final String STOP_IN_SPAWN_COMMENT = "Stop the time for players in an area around spawn.";
	public static boolean stopInSpawn;

	private static final String STOP_IN_SPAWN_RADIUS_KEY = "6 Timestop radius";
	private static final int STOP_IN_SPAWN_RADIUS_DEFAULT = 20;
	private static final String STOP_IN_SPAWN_RADIUS_COMMENT = "The radius of the spawn-time-stop in blocks.";
	public static int stopInSpawnRadius;

	/* ————————————————————————————————————————————————————— */

	private static final String CAT_DEBUG_KEY = "7_Debug";
	private static final String CAT_DEBUG_COMMENT = "Debug logging options.\nYou shouldn't need these, I just kept most of them in the code for the heck of it... O: )";

	private static final String DEBUG_LOGGING_KEY = "1 Debug logging";
	private static final boolean DEBUG_LOGGING_DEFAULT = false;
	public static boolean debugLogging;

	private static final String EXCESSIVE_LOGGING_KEY = "2 Excessive debug logging";
	private static final boolean EXCESSIVE_LOGGING_DEFAULT = false;
	private static final String EXCESSIVE_LOGGING_COMMENT = "This will really spam your console, only activate if instructed to.";
	public static boolean excessiveLogging;

	private static final String CONSTANT_EXCESSIVE_LOGGING_KEY = "3 Constant excessive debug logging";
	private static final boolean CONSTANT_EXCESSIVE_LOGGING_DEFAULT = false;
	private static final String CONSTANT_EXCESSIVE_LOGGING_COMMENT = "This will really, really, really spam your console, only activate if instructed to.";
	public static boolean constantExcessiveLogging;

	// TODO Variable time reset

	/*
	 * private static final String = ; private static final int = ; private static final String = ; public static int ;
	 */

	private static Configuration config;

	/**
	 * Initialize PvPUtils config
	 * 
	 * @param file Config file
	 */
	public static void init(File file) {
		// If the "config"-Variable is not yet initialized...
		if (config == null){
			config = new Configuration(file);
			loadConfig();
			if (debugLogging)
				Logger.info("Config initilized");
		}
	}

	/**
	 * Load PvPUtils config <br>
	 * Initialize config-related variables
	 */
	private static void loadConfig() {
		config.load();

		/* ————————————————————————————————————————————————————— */

		// Modules
		// Variable definitions
		config.setCategoryComment(CAT_MODULES_KEY, CAT_MODULES_COMMENT);

		cmdlogEnabled = config.get(CAT_MODULES_KEY, CMDLOG_ENABLED_KEY, CMDLOG_ENABLED_DEFAULT, CMDLOG_ENABLED_COMMENT).getBoolean();
		macroDisable = config.get(CAT_MODULES_KEY, MACRO_DISABLE_KEY, MACRO_DISABLE_DEFAULT, MACRO_DISABLE_COMMENT).getBoolean();
		xpLockEnabled = config.get(CAT_MODULES_KEY, XPLOCK_DISABLE_KEY, XPLOCK_DISABLE_DEFAULT, XPLOCK_DISABLE_COMMENT).getBoolean();
		livesEnabled = config.get(CAT_MODULES_KEY, LIVES_ENABLED_KEY, LIVES_ENABLED_DEFAULT, lives_ENABLED_COMMENT).getBoolean();
		timeEnabled = config.get(CAT_MODULES_KEY, TIME_ENABLED_KEY, TIME_ENABLED_DEFAULT, TIME_ENABLED_COMMENT).getBoolean();

		/* ————————————————————————————————————————————————————— */

		// Command Log
		// Variable definition & error handling
		config.setCategoryComment(CAT_COMMAND_LOG_KEY, CAT_COMMAND_LOG_COMMENT);

		cmdlogWhere = config.get(CAT_COMMAND_LOG_KEY, CMDLOG_WHERE_KEY, CMDLOG_WHERE_DEFAULT, CMDLOG_WHERE_COMMENT).getInt();
		if (cmdlogWhere < 1 || cmdlogWhere > 3){
			Logger.warn("Config Error: \"" + CMDLOG_WHERE_KEY + "\" not in defined area! Read the Comment ; ) - Setting to default value!");
			cmdlogWhere = CMDLOG_WHERE_DEFAULT;
			config.get(CAT_COMMAND_LOG_KEY, CMDLOG_WHERE_KEY, CMDLOG_WHERE_DEFAULT, CMDLOG_WHERE_COMMENT).set(cmdlogWhere);
		}

		/* ————————————————————————————————————————————————————— */

		// Macros
		// Variable definition & error handling
		config.setCategoryComment(CAT_MACROS_KEY, CAT_MACROS_COMMENT);

		macroTreshold = config.get(CAT_MACROS_KEY, MACRO_TRESHOLD_KEY, MACRO_TRESHOLD_DEFAULT, MACRO_TRESHOLD_COMMENT).getInt();
		if (macroTreshold < 1){
			Logger.warn("Config Error: '" + MACRO_TRESHOLD_KEY + "' < 1! Setting to last usable number!");
			macroTreshold = 1;
			config.get(CAT_MACROS_KEY, MACRO_TRESHOLD_KEY, MACRO_TRESHOLD_DEFAULT, MACRO_TRESHOLD_COMMENT).set(macroTreshold);
		}

		macroKicker = config.get(CAT_MACROS_KEY, MACRO_KICKER_KEY, MACRO_KICKER_DEFAULT, MACRO_KICKER_COMMENT).getBoolean();
		if (!macroDisable && macroKicker){
			Logger.warn("Config Error: \"" + MACRO_KICKER_KEY + "\" enabled although \"" + MACRO_DISABLE_KEY + "\" is disabled... Setting \"" + MACRO_KICKER_KEY
					+ "\" to \"false\"");
			macroKicker = false;
			config.get(CAT_MACROS_KEY, MACRO_KICKER_KEY, MACRO_KICKER_DEFAULT, MACRO_KICKER_COMMENT).set(macroKicker);
		}

		macroKickerTreshold = config.get(CAT_MACROS_KEY, MACRO_KICKER_TRESHOLD_KEY, MACRO_KICKER_TRESHOLD_DEFAULT, MACRO_KICKER_TRESHOLD_COMMENT).getInt();
		if (macroKickerTreshold < 1){
			Logger.warn("Config Error: '" + MACRO_KICKER_TRESHOLD_KEY + "' < 1! Setting to last usable number!");
			macroKickerTreshold = 1;
			config.get(CAT_MACROS_KEY, MACRO_KICKER_TRESHOLD_KEY, MACRO_KICKER_TRESHOLD_DEFAULT, MACRO_KICKER_TRESHOLD_COMMENT).set(macroKickerTreshold);
		}

		/* ————————————————————————————————————————————————————— */

		// XP
		// Variable definition
		config.setCategoryComment(CAT_XP_KEY, CAT_XP_COMMENT);
		xpLockLevel = config.get(CAT_XP_KEY, XPLOCK_LEVEL_KEY, XPLOCK_LEVEL_DEFAULT, XPLOCK_LEVEL_COMMENT).getInt();

		/* ————————————————————————————————————————————————————— */

		// Lives
		// Variable definition & error handling
		config.setCategoryComment(CAT_LIVES, CAT_LIVES_COMMENT);

		int baseStartlives = config.get(CAT_LIVES, LIVES_KEY, LIVES_DEFAULT, LIVES_COMMENT).getInt();
		if (baseStartlives < 1){
			Logger.warn("Config Error: '" + LIVES_KEY + "' < 1! Settting to last usable number!");
			baseStartlives = 1;
			config.get(CAT_LIVES, LIVES_KEY, LIVES_DEFAULT, LIVES_COMMENT).set(baseStartlives);
		}

		int baseMaxlives = config.get(CAT_LIVES, MAXLIVES_KEY, MAXLIVES_DEFAULT, MAXLIVES_COMMENT).getInt();
		if (baseMaxlives < 1){
			Logger.warn("Config Error: '" + MAXLIVES_KEY + "' < 1! Setting to last usable number!");
			baseMaxlives = 1;
			config.get(CAT_LIVES, MAXLIVES_KEY, MAXLIVES_DEFAULT, MAXLIVES_COMMENT).set(baseMaxlives);
		}
		else if (baseMaxlives > 255){
			Logger.warn("Config Error: '" + MAXLIVES_KEY + "' > 255! Setting to last usable number!");
			baseMaxlives = 255;
			config.get(CAT_LIVES, MAXLIVES_KEY, MAXLIVES_DEFAULT, MAXLIVES_COMMENT).set(baseMaxlives);
		}
		if (baseMaxlives < baseStartlives){
			Logger.warn("Config Error: '" + MAXLIVES_KEY + "' < '" + LIVES_KEY + "'! Setting '" + MAXLIVES_KEY + "' equal to '" + LIVES_KEY + "'!");
			baseMaxlives = baseStartlives;
			config.get(CAT_LIVES, MAXLIVES_KEY, MAXLIVES_DEFAULT, MAXLIVES_COMMENT).set(baseMaxlives);
		}
		startLives = (byte) baseStartlives;
		maxLives = (byte) baseMaxlives;

		livesTakenBy = config.get(CAT_LIVES, LIVES_TAKEN_KEY, LIVES_TAKEN_DEFAULT, LIVES_TAKEN_COMMENT).getInt();
		if (livesTakenBy < 1 || livesTakenBy > 3){
			Logger.warn("Config Error: '" + LIVES_TAKEN_KEY + "' not in defined area! Read the Comment ; ) Setting to default value!");
			livesTakenBy = LIVES_TAKEN_DEFAULT;
			config.get(CAT_LIVES, LIVES_TAKEN_KEY, LIVES_TAKEN_DEFAULT, LIVES_TAKEN_COMMENT).set(livesTakenBy);
		}

		noLifeChat = config.get(CAT_LIVES, NO_LIFE_CHAT_KEY, NO_LIFE_CHAT_DEFAULT, NO_LIFE_CHAT_COMMENT).getBoolean();
		scoreboardEnabled = config.get(CAT_LIVES, SCOREBOARD_ENABLED_KEY, SCOREBOARD_ENABLED_DEFAULT, SCOREBOARD_ENABLED_COMMENT).getBoolean();

		scoreboardType = config.get(CAT_LIVES, SCOREBOARD_TYPE_KEY, SCOREBOARD_TYPE_DEFAULT, SCOREBOARD_TYPE_COMMENT).getInt();
		if (scoreboardType < 1 || scoreboardType > 3){
			Logger.warn("Config Error: '" + SCOREBOARD_TYPE_KEY + "' not in defined area! Read the Comment ; ) Setting to default value!");
			scoreboardType = SCOREBOARD_TYPE_DEFAULT;
			config.get(CAT_LIVES, SCOREBOARD_TYPE_KEY, SCOREBOARD_TYPE_DEFAULT, SCOREBOARD_TYPE_COMMENT).set(scoreboardType);
		}
		scoreboardType -= 1;

		/* ————————————————————————————————————————————————————— */

		// Time
		// Variable definition & error handling
		config.setCategoryComment(CAT_TIME, CAT_TIME_COMMENT);

		addedTime = config.get(CAT_TIME, ADD_TIME_KEY, ADD_TIME_DEFAULT, ADD_TIME_COMMENT).getInt() * 60;
		if (addedTime < 0){
			Logger.warn("Config Error: '" + ADD_TIME_KEY + "' < 0! Setting to last usable number!");
			addedTime = 0;
			config.get(CAT_TIME, ADD_TIME_KEY, ADD_TIME_DEFAULT, ADD_TIME_COMMENT).set(addedTime);
		}

		startTime = config.get(CAT_TIME, START_TIME_KEY, START_TIME_DEFAULT, START_TIME_COMMENT).getInt() * 60;
		if (startTime < 1){
			Logger.warn("Config Error: '" + START_TIME_KEY + "' < 1! Setting to last usable number!");
			startTime = 1;
			config.get(CAT_TIME, START_TIME_KEY, START_TIME_DEFAULT, START_TIME_COMMENT).set(startTime);
		}

		maxTime = config.get(CAT_TIME, MAXTIME_KEY, MAXTIME_DEFAULT, MAXTIME_COMMENT).getInt() * 60;
		if (maxTime < 1){
			Logger.warn("Config Error: '" + MAXTIME_KEY + "' < 1! Setting to last usable number!");
			maxTime = 1;
			config.get(CAT_TIME, MAXTIME_KEY, MAXTIME_DEFAULT, MAXTIME_COMMENT).set(maxTime);
		}

		if (maxTime < addedTime){
			Logger.warn("Config Error: '" + MAXTIME_KEY + "' < '" + ADD_TIME_KEY + "'! Setting '" + MAXTIME_KEY + "' equal to '" + ADD_TIME_KEY + "'!");
			maxTime = addedTime;
			config.get(CAT_TIME, MAXTIME_KEY, MAXTIME_DEFAULT, MAXTIME_COMMENT).set(maxTime);
		}

		if (maxTime < startTime){
			Logger.warn("Config Error: '" + MAXTIME_KEY + "' < '" + START_TIME_KEY + "'! Setting '" + MAXTIME_KEY + "' equal to '" + START_TIME_KEY + "'!");
			maxTime = startTime;
			config.get(CAT_TIME, MAXTIME_KEY, MAXTIME_DEFAULT, MAXTIME_COMMENT).set(maxTime);
		}

		noTimeChat = config.get(CAT_TIME, NO_TIME_CHAT_KEY, NO_TIME_CHAT_DEFAULT, NO_TIME_CHAT_COMMENT).getBoolean();
		stopInSpawn = config.get(CAT_TIME, STOP_IN_SPAWN_KEY, STOP_IN_SPAWN_DEFAULT, STOP_IN_SPAWN_COMMENT).getBoolean();
		stopInSpawnRadius = config.get(CAT_TIME, STOP_IN_SPAWN_RADIUS_KEY, STOP_IN_SPAWN_RADIUS_DEFAULT, STOP_IN_SPAWN_RADIUS_COMMENT).getInt();

		/* ————————————————————————————————————————————————————— */

		// Debug
		// Variable definition
		config.setCategoryComment(CAT_DEBUG_KEY, CAT_DEBUG_COMMENT);

		debugLogging = config.get(CAT_DEBUG_KEY, DEBUG_LOGGING_KEY, DEBUG_LOGGING_DEFAULT).getBoolean();
		excessiveLogging = config.get(CAT_DEBUG_KEY, EXCESSIVE_LOGGING_KEY, EXCESSIVE_LOGGING_DEFAULT, EXCESSIVE_LOGGING_COMMENT).getBoolean();
		constantExcessiveLogging = config
				.get(CAT_DEBUG_KEY, CONSTANT_EXCESSIVE_LOGGING_KEY, CONSTANT_EXCESSIVE_LOGGING_DEFAULT, CONSTANT_EXCESSIVE_LOGGING_COMMENT).getBoolean();

		/* ————————————————————————————————————————————————————— */

		// The only code ACTUALLY creating the config ;)
		if (config.hasChanged())
			config.save();
	}
}
