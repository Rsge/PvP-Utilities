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
		sendChat(cmdsender, "/" + Reference.MODID + " version");
		if (isCmdsAllowed(cmdsender))
		{
			sendChat(cmdsender, "/" + Reference.MODID + " save");
			sendChat(cmdsender, "/" + Reference.MODID + " reload");
			if (Config.livesEnabled)
			{
				sendChat(cmdsender, "/" + Reference.MODID + " lives <playername/uuid/enable/disable/reset/add/remove>");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [reset/add/remove] [playername/uuid/all]");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [set/add/remove] [amount]");
				sendChat(cmdsender, "/" + Reference.MODID + " lives [set/add/remove] [playername/uuid/all] [amount]");
			}
			if (Config.timeEnabled)
			{
				sendChat(cmdsender, "/" + Reference.MODID + " time <playername/uuid/enable/disable/reset/add/remove/start/stop>");
				sendChat(cmdsender, "/" + Reference.MODID + " time [reset/add/remove/start/stop] [playername]");
				sendChat(cmdsender, "/" + Reference.MODID + " time [reset/add/remove] [uuid/all]");
				sendChat(cmdsender, "/" + Reference.MODID + " time [set/add/remove] [amount in minutes]");
				sendChat(cmdsender, "/" + Reference.MODID + " time [set/add/remove] [playername/uuid/all] [amount in minutes]");
				sendChat(cmdsender, "/" + Reference.MODID + " time set multiplier [percentage]");
				sendChat(cmdsender, "/" + Reference.MODID + " time set multiplier [playername/uuid/all] [percentage]");
			}
		}
		else
		{
			if (Config.livesEnabled)
				sendChat(cmdsender, "/" + Reference.MODID + " lives <playername/uuid>");
			if (Config.timeEnabled)
				sendChat(cmdsender, "/" + Reference.MODID + " time <playername/uuid>");
		}

	}
}
