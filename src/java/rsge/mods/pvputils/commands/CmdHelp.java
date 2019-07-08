package rsge.mods.pvputils.commands;

import net.minecraft.command.ICommandSender;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Reference;


/**
 * Help command
 * 
 * @author Rsge
 */
public class CmdHelp extends CmdBase
{
	public CmdHelp()
	{
		super("help");
		permissionLevel = -1;
	}

	/* ————————————————————————————————————————————————————— */

	// Overrides
	@Override
	public boolean isVisible(ICommandSender cmdsender)
	{
		return true;
	}

	@Override
	public int getPermissionLevel()
	{
		return -1;
	}

	@Override
	public void handleCommand(ICommandSender cmdsender, String[] args)
	{
		if (isCmdsAllowed(cmdsender))
		{
			sendChat(cmdsender, "/" + Reference.MODID + " version");
			sendChat(cmdsender, "/" + Reference.MODID + " reload");
			if (Config.livesEnabled)
			{
				sendChat(cmdsender, "/" + Reference.MODID + " lives <player/reset/add/remove/enable/disable>");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [reset/add/remove] [player]");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [set/add/remove] [amount]");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [set/add/remove] [player] [amount]");
			}
			if (Config.timeEnabled)
			{
				sendChat(cmdsender, "/" + Reference.MODID + " time <player/reset/add/remove/start/stop/enable/disable>");
				sendChat(cmdsender, "/" + Reference.MODID + " time [reset/add/remove/start/stop] [player]");
				sendChat(cmdsender, "/" + Reference.MODID + " time [set/add/remove] [amount in minutes]");
				sendChat(cmdsender, "/" + Reference.MODID + " time [set/add/remove] [player] [amount in minutes]");
			}
		}
		else
		{
			sendChat(cmdsender, "/" + Reference.MODID + " version");
			if (Config.livesEnabled)
				sendChat(cmdsender, "/" + Reference.MODID + " lives <player>");
			if (Config.timeEnabled)
				sendChat(cmdsender, "/" + Reference.MODID + " time <player>");
		}

	}
}
