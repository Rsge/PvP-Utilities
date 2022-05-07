package rsge.mods.pvputils.main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.command.ICommandSender;


/**
 * Logging class
 * 
 * @author Rsge
 */
public class Logger {
	/**
	 * Basic logging command
	 * 
	 * @param logLevel Level of logging
	 * @param object   to be logged
	 */
	private static void log(Level logLevel, Object object) {
		FMLLog.log(Reference.NAME, logLevel, String.valueOf(object));
	}

	/**
	 * Logging of worst error ever
	 * 
	 * @param object to be logged
	 */
	public static void off(Object object) {
		log(Level.OFF, object);
	}

	/**
	 * Logging of fatal errors
	 * 
	 * @param object to be logged
	 */
	public static void fatal(Object object) {
		log(Level.FATAL, object);
	}

	/**
	 * Logging of normal errors
	 * 
	 * @param object to be logged
	 */
	public static void error(Object object) {
		log(Level.ERROR, object);
	}

	/**
	 * Logging of warnings
	 * 
	 * @param object to be logged
	 */
	public static void warn(Object object) {
		log(Level.WARN, object);
	}

	/**
	 * Logging of informations
	 * 
	 * @param object to be logged
	 */
	public static void info(Object object) {
		log(Level.INFO, object);
	}

	/**
	 * Debug logging
	 * 
	 * @param object to be logged
	 */
	public static void debug(Object object) {
		log(Level.DEBUG, object);
	}

	/**
	 * Trace logging
	 * 
	 * @param object to be logged
	 */
	public static void trace(Object object) {
		log(Level.TRACE, object);
	}

	/**
	 * All logging not covered by other log cmds
	 * 
	 * @param object to be logged
	 */
	public static void all(Object object) {
		log(Level.ALL, object);
	}

	/**
	 * Special logging for PvP Utilities commands
	 * 
	 * @param cmdsender Sender of command
	 * @param s         String to log
	 */
	public static void logCmd(ICommandSender cmdsender, String s) {
		try{
			DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);
			String l = "[" + LocalDateTime.now().format(dtf) + "] [" + cmdsender.getCommandSenderName() + "] [" + s + "]";
			List<String> log = Arrays.asList(l);

			Files.write(Paths.get(Reference.loggedCmds.getAbsolutePath()), log, StandardOpenOption.APPEND);
		}
		catch (Exception ex){
			Logger.error("ERROR while trying to log a PvP-Utilities-command");
		}
	}
}
