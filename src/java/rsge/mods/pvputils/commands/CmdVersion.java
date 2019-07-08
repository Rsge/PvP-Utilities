package rsge.mods.pvputils.commands;

import net.minecraft.command.ICommandSender;
import rsge.mods.pvputils.main.Reference;


/**
 * Chat version
 * 
 * @author Rsge
 */
public class CmdVersion extends CmdBase
{
	public CmdVersion()
	{
		super("version");
		permissionLevel = 0;
	}

	@Override
	public void handleCommand(ICommandSender cmdsender, String[] s)
	{
		// TODO Translation
		sendChat(cmdsender, "Currently using "/* "pvputils.message.version.1" */ + Reference.VERSION + " of PvP Utilities"/* "pvputils.message.version.2" */);
	}
}
