package rsge.mods.pvputils.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.data.Time;


/**
 * Reload command <br>
 * Re-initializes lives & time
 * 
 * @author Rsge
 */
public class CmdReload extends CmdBase
{
	public CmdReload()
	{
		super("reload");
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
		try
		{
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			if (Config.livesEnabled)
				Lives.init();
			if (Config.timeEnabled)
				Time.init();

			sendChat(cmdsender, "Data reloaded");
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
