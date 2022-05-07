package rsge.mods.pvputils.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import rsge.mods.pvputils.config.Config;


/**
 * "pvputils" base command
 * 
 * @author Rsge
 */
public class CmdHandler extends CommandBase {

	public static Map<String, ISubCmd> commands = new LinkedHashMap<>();
	public static CmdHandler instance = new CmdHandler();

	/* ————————————————————————————————————————————————————— */

	/**
	 * Registering a command to command registry
	 * 
	 * @param cmd to register
	 */
	public static void register(ISubCmd cmd) {
		commands.put(cmd.getCommandName(), cmd);
	}

	/**
	 * Checking a commands existence
	 * 
	 * @param  name of command
	 * @return      if command is registered
	 */
	public static boolean commandExists(String name) {
		return commands.containsKey(name);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * List of all commands after base command to register
	 */
	static{
		register(new CmdHelp());
		register(new CmdVersion());
		if (Config.livesEnabled || Config.timeEnabled){
			register(new CmdSave());
			register(new CmdReload());
			if (Config.livesEnabled)
				register(new CmdLives());
			if (Config.timeEnabled)
				register(new CmdTime());
		}
	}

	/* ————————————————————————————————————————————————————— */

	// Overrides standard setting:
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cmdsender) {
		// Everyone can use this command
		return true;
	}

	/**
	 * @return Name of command
	 */
	@Override
	public String getCommandName() {
		return "pvputils";
	}

	private List<String> alias;

	/**
	 * @return Aliases of command
	 */
	public List<String> getCommandAliases() {
		this.alias = new ArrayList<String>();
		this.alias.add("pvputils");
		return this.alias;
	}

	/**
	 * @return "/[Command name] help"
	 */
	public String getCommandUsage(ICommandSender cmdsender) {
		return "/" + getCommandName() + " help";
		// ==> /pvputils help
	}

	@Override
	@SuppressWarnings(value = {"unchecked", "rawtypes"})
	public List addTabCompletionOptions(ICommandSender cmdsender, String[] args) {
		if (args.length == 1){
			// "s" is given the value of "args" at the position 0
			String s = args[0];
			List res = new ArrayList();
			// Goes through each command in the "commands"-list
			for (ISubCmd cmd : commands.values()){
				// If the command is visible to the sender and starts with "s"
				if (cmd.isVisible(cmdsender) && cmd.getCommandName().startsWith(s))
					// adds it to the results-list
					res.add(cmd.getCommandName());
			}
			return res;
		}
		else if (commands.containsKey(args[0]) && commands.get(args[0]).isVisible(cmdsender)){
			return commands.get(args[0]).addTabCompletionOptions(cmdsender, Arrays.copyOfRange(args, 1, args.length));
		}
		return null;
	}

	/**
	 * Process the command
	 * 
	 * @param cmdsender Player who send command
	 * @param args      Arguments of command
	 */
	public void processCommand(ICommandSender cmdsender, String[] args) {
		// If there are no arguments
		if (args.length < 1){
			// starts the help command
			args = new String[] {"help"};
		}
		ISubCmd cmd = commands.get(args[0]);
		if (cmd != null){
			if (cmd.isVisible(cmdsender) && (cmdsender.canCommandSenderUseCommand(cmd.getPermissionLevel(), getCommandName() + " " + cmd.getCommandName())
					|| (cmdsender instanceof EntityPlayerMP && cmd.getPermissionLevel() <= 0))){
				cmd.handleCommand(cmdsender, Arrays.copyOfRange(args, 1, args.length));
				return;
			}
			throw new CommandException("pvputils.command.noPermission");
		}
		throw new CommandNotFoundException("pvputils.command.notFound");
	}
}
