package rsge.mods.pvputils.commands;

import java.util.List;

import net.minecraft.command.ICommandSender;


/**
 * Subcommands
 * 
 * @author Rsge
 */
public interface ISubCmd
{
	/**
	 * @return Permission level of command as integer
	 */
	public int getPermissionLevel();

	/**
	 * @return Name of command
	 */
	public String getCommandName();

	/**
	 * Process the command
	 * 
	 * @param cmdsender Player who send command
	 * @param args      Arguments of command
	 */
	public void handleCommand(ICommandSender cmdsender, String[] args);

	/**
	 * Adds options to Tab-autocompletion
	 * 
	 * @param  cmdsender Player who presses tab
	 * @param  s         Startstring
	 * @return           Autocompletion options
	 */
	public List<String> addTabCompletionOptions(ICommandSender cmdsender, String[] s);

	/**
	 * Is command visible for player in autocompletion
	 * 
	 * @param  cmdsender Player who tries to autocomplete
	 * @return           if sender can see command
	 */
	public boolean isVisible(ICommandSender cmdsender);

	/**
	 * Get syntax options of command
	 * 
	 * @param  cmdsender Player using command
	 * @return           Syntax options for command
	 */
	public int[] getSyntaxOptions(ICommandSender cmdsender);
}
