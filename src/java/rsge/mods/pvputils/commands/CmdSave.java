package rsge.mods.pvputils.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.data.Time;


/**
 * Save command <br>
 * Save data of lives & time
 * 
 * @author Rsge
 */
public class CmdSave extends CmdBase
{
	public CmdSave()
	{
		super("save");
		permissionLevel = 3;
	}

	// Overrides
	@Override
	public boolean isVisible(ICommandSender cmdsender)
	{
		if (isCmdsAllowed(cmdsender))
			return true;
		else
			return false;
	}

	@Override
	public int getPermissionLevel()
	{
		return 3;
	}

	@Override
	public void handleCommand(ICommandSender cmdsender, String[] args)
	{
		if (!isCmdsAllowed(cmdsender))
			throw new CommandException("pvputils.command.noPermission");

		if (Config.livesEnabled)
			Lives.save();
		if (Config.timeEnabled)
			Time.save();

		sendChat(cmdsender, "Data Saved");
	}
}
