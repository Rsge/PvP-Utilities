package rsge.mods.pvputils.listeners;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;
import rsge.mods.pvputils.main.Reference;


/**
 * Logging of commands
 * 
 * @author Rsge
 */
public class CommandEventListener {
	public CommandEventListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Logging of relevant commands
	 * 
	 * @param e Command event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCmd(CommandEvent e) {
		boolean unimportant = e.command.getCommandName().startsWith(Reference.MODID) || e.command.getCommandName().contains("help")
				|| e.command.getCommandName().startsWith("list") || e.command.getCommandName().startsWith("save")
				|| e.command.getCommandName().startsWith("say") || e.command.getCommandName().startsWith("tell")
				|| e.command.getCommandName().startsWith("whisper") || e.command.getCommandName().startsWith("ping")
				|| e.command.getCommandName().startsWith("rules");

		if (Config.cmdlogWhere == 1){
			logToConsole(e, unimportant);
			logToFile(e, unimportant);
		}
		else if (Config.cmdlogWhere == 2)
			logToConsole(e, unimportant);
		else if (Config.cmdlogWhere == 3)
			logToFile(e, unimportant);
	}

	/**
	 * Log commands to Minecraft console
	 * 
	 * @param e           Command event
	 * @param unimportant Boolean, if command is too unimportant to log
	 */
	private void logToConsole(CommandEvent e, boolean unimportant) {
		if (!unimportant){
			String log = "Player \"" + e.sender.getCommandSenderName() + "\" used command \"/" + e.command.getCommandName();
			for (int i = 0; i < e.parameters.length; i++){
				log += (" " + e.parameters[i]);
			}
			log += "\"";
			Logger.info(log);
		}

	}

	/**
	 * Log commands to separate file
	 * 
	 * @param e           Command event
	 * @param unimportant Boolean, if command is too unimportant to log
	 */
	private void logToFile(CommandEvent e, boolean unimportant) {
		if (!unimportant){
			try (BufferedWriter bw = Files.newBufferedWriter(Reference.loggedCmds.toPath(), StandardOpenOption.APPEND)){
				DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.UK);

				String l = "";
				for (int i = 0; i < e.parameters.length; i++){
					l += (" " + e.parameters[i]);
				}

				String log = "[" + LocalDateTime.now().format(dtf) + "] [" + e.sender.getCommandSenderName() + "] [/" + e.command.getCommandName() + l + "]\n";

				bw.write(log);
			}
			catch (Exception ex){
				Logger.error("ERROR trying to log a command");
			}
		}
	}
}
