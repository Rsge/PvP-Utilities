package rsge.mods.pvputils.commands;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.data.ScoreBoard;
import rsge.mods.pvputils.data.Time;
import rsge.mods.pvputils.main.Logger;


/**
 * Time base command
 * 
 * @author Rsge
 */
public class CmdTime extends CmdBase
{
	public CmdTime()
	{
		super("time", "enable", "disable", "start", "stop", "reset", "add", "remove");
		permissionLevel = 0;
	}

	/* ————————————————————————————————————————————————————— */

	// Overrides
	@Override
	public boolean isVisible(ICommandSender cmdsender)
	{
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender cmdsender, String[] args)
	{
		if (isCmdsAllowed(cmdsender))
			return super.addTabCompletionOptions(cmdsender, args);
		else
			return null;
	}

	@Override
	public int[] getSyntaxOptions(ICommandSender cmdsender)
	{
		return isCmdsAllowed(cmdsender) ? new int[] {0, 1, 2, 3} : super.getSyntaxOptions(cmdsender);
	}

	/* ————————————————————————————————————————————————————— */

	@Override
	public void handleCommand(ICommandSender cmdsender, String[] args)
	{
		int a;
		// With no extra arguments
		// /pvputils time
		if (args.length == 0 && cmdsender instanceof EntityPlayer)
		{
			EntityPlayerMP p = (EntityPlayerMP) cmdsender;
			Time.chatTime(p);
		}

		// With 1 argument
		// /pvputils time <enable/disable/start/stop/reset/add/remove/playername>
		else if (args.length == 1 && cmdsender instanceof EntityPlayer)
		{
			if (args[0].equalsIgnoreCase("enable"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					Config.timeEnabled = true;

					// Log command
					String s = "Enabled time.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
				}
			}

			else if (args[0].equalsIgnoreCase("disable"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					Config.timeEnabled = false;

					// Log command
					String s = "Disabled time";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
				}
			}

			else if (args[0].equalsIgnoreCase("start"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					EntityPlayerMP p = (EntityPlayerMP) cmdsender;
					Time.startTime(p);

					// Log command
					String s = "Started his/her time.";
					sendChat(cmdsender, "Started your time.");
					Logger.logCmd(cmdsender, s);
				}
			}
			else if (args[0].equalsIgnoreCase("stop"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					EntityPlayerMP p = (EntityPlayerMP) cmdsender;
					Time.stopTime(p);

					// Log command
					String s = "Stopped his/her time.";
					sendChat(cmdsender, "Stopped your time.");
					Logger.logCmd(cmdsender, s);
				}
			}
			else if (args[0].equalsIgnoreCase("reset"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					EntityPlayerMP p = (EntityPlayerMP) cmdsender;
					Time.resetTime(p);

					// Log command
					String s = "Reset his/her time.";
					sendChat(cmdsender, "Reset your time.");
					Logger.logCmd(cmdsender, s);
				}
			}
			else if (args[0].equalsIgnoreCase("add"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					EntityPlayerMP p = (EntityPlayerMP) cmdsender;
					Time.addTime(p);

					// Log command
					String s = "Added " + Config.addedTime / 60 + " minutes to him-/herself.";
					if (!Time.tooMuchAdded)
						sendChat(cmdsender, "Added " + Config.addedTime / 60 + " minutes to yourself.");
					else
					{
						sendChat(cmdsender, "You reached the max possible time.");
						Time.tooMuchAdded = false;
					}
					Logger.logCmd(cmdsender, s);
				}

			}
			else if ((args[0]).equalsIgnoreCase("remove"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");
				else
				{
					EntityPlayerMP p = (EntityPlayerMP) cmdsender;
					Time.removeTime(p);

					// Log command
					String s = "Removed " + Config.addedTime / 60 + " minutes from him-/herself.";
					sendChat(cmdsender, "Removed " + Config.addedTime / 60 + " minutes from yourself.");
					Logger.logCmd(cmdsender, s);
				}
			}
			else
			{
				try
				{
					String end;
					UUID uuid = UUID.fromString(args[0]);
					byte lives = Lives.getLives(uuid);
					if (lives == 1)
						end = "life left.";
					else
						end = "lives left.";
					cmdsender.addChatMessage(new ChatComponentText("This guy has " + lives + end));
				}
				catch (IllegalArgumentException ex)
				{
					String name = args[0];
					try
					{
						EntityPlayer p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						EntityPlayer receiver = (EntityPlayer) cmdsender;
						Time.chatTimeTo(p, receiver, false);
					}
					catch (Exception exc)
					{
						throw new WrongUsageException("pvputils.command.realPlayer");
					}
				}
			}
		}

		// With 2 arguments
		// /pvputils time [reset/set/add/remove] [playername/amont]
		else if (args.length == 2 && cmdsender instanceof EntityPlayer)
		{
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");
			else
			{
				try
				{
					// /pvputils time [set/add/remove] [amont]
					if (StringUtils.isNumeric(args[1]))
					{
						a = Integer.parseInt(args[1]) * 60;

						if (a < 0)
							throw new WrongUsageException("pvputils.command.positiveNumbers");

						if (args[0].equalsIgnoreCase("set"))
						{
							EntityPlayerMP p = (EntityPlayerMP) cmdsender;
							Time.setTime(p, a);

							// Log command
							String s = "Set his/her time to " + a / 60 + " minute(s).";
							if (!Time.tooMuchAdded)
								sendChat(cmdsender, "Set your time to " + a / 60 + " minute(s).");
							else
							{
								sendChat(cmdsender, "You reached the max amount of time.");
								Time.tooMuchAdded = false;
							}
							Logger.logCmd(cmdsender, s);
						}
						else if (args[0].equalsIgnoreCase("add"))
						{
							EntityPlayerMP p = (EntityPlayerMP) cmdsender;
							Time.addTime(p, a);

							// Log command
							String s = "Added " + a / 60 + " minute(s) to him-/herself.";
							if (!Time.tooMuchAdded)
								sendChat(cmdsender, "Added " + a / 60 + " minute(s) to yourself.");
							else
							{
								sendChat(cmdsender, "You reached the max amount of time.");
								Time.tooMuchAdded = false;
							}
							Logger.logCmd(cmdsender, s);
						}
						else if (args[0].equalsIgnoreCase("remove"))
						{
							EntityPlayerMP p = (EntityPlayerMP) cmdsender;
							Time.removeTime(p, a);

							// Log command
							String s = "Removed " + a / 60 + " minute(s) from him-/herself.";
							sendChat(cmdsender, "Removed " + a / 60 + " minute(s) from yourself.");
							Logger.logCmd(cmdsender, s);
						}
					}
					// /pvputils time [reset/add/remove] [playername]
					else
					{
						String name = args[1];

						if (args[0].equalsIgnoreCase("start"))
						{
							EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
							Time.startTime(p);

							// Log command
							String s = "Started " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
						}
						if (args[0].equalsIgnoreCase("stop"))
						{
							EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
							Time.stopTime(p);

							// Log command
							String s = "Stopped " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
						}
						if (args[0].equalsIgnoreCase("reset"))
						{
							EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
							Time.resetTime(p);

							// Log command
							String s = "Reset " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
						}
						else if (args[0].equalsIgnoreCase("add"))
						{
							EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
							Time.addTime(p);

							// Log command
							String s = "Added " + Config.addedTime / 60 + " minutes to " + name + ".";
							if (!Time.tooMuchAdded)
								sendChat(cmdsender, s);
							else
							{
								sendChat(cmdsender, name + " has reached the max amount of time.");
								Time.tooMuchAdded = false;
							}
							Logger.logCmd(cmdsender, s);
						}
						else if (args[0].equalsIgnoreCase("remove"))
						{
							EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
							Time.removeTime(p);

							// Log command
							String s = "Removed " + Config.addedTime / 60 + " minutes from " + name + ".";
							if (!Time.playerBanned)
								sendChat(cmdsender, s);
							else
							{
								sendChat(cmdsender, "Removed remaining time from " + name + ". Player kicked!");
								Time.playerBanned = false;
							}
							Logger.logCmd(cmdsender, s);
						}
					}
				}
				catch (Exception ex)
				{
					throw new WrongUsageException("pvputils.command.realPlayer");
				}
			}

			if (Config.scoreboardEnabled && cmdsender instanceof EntityPlayer)
				ScoreBoard.updatePlayer((EntityPlayer) cmdsender);
		}

		// With all arguments
		// /pvputils time <set/add/remove> <playername> <amont>
		else if (args.length == 3 && cmdsender instanceof EntityPlayer)
		{
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");
			else
			{
				try
				{
					String name = args[1];
					a = Integer.parseInt(args[2]) * 60;

					if (a < 0)
						throw new WrongUsageException("pvputils.command.positiveNumbers");

					if (args[0].equalsIgnoreCase("set"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Time.setTime(p, a);

						// Log command
						String s = "Set " + name + "'s time to " + a / 60 + " minute(s).";
						if (!Time.tooMuchAdded)
							sendChat(cmdsender, s);
						else
						{
							sendChat(cmdsender, name + " reached the max possible time.");
							Time.tooMuchAdded = false;
						}
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("add"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Time.addTime(p, a);

						// Log command
						String s = "Added " + a / 60 + " minute(s) to " + name + ".";
						if (!Time.tooMuchAdded)
							sendChat(cmdsender, s);
						else
						{
							sendChat(cmdsender, name + " reached the max possible time.");
							Time.tooMuchAdded = false;
						}
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("remove"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Time.removeTime(p, a);

						// Log command
						String s = "Removed " + a / 60 + " minute(s) from " + name + ".";
						if (!Time.playerBanned)
							sendChat(cmdsender, s);
						else
						{
							sendChat(cmdsender, "Removed remaining time from " + name + ". Player kicked!");
							Time.playerBanned = false;
						}
						Logger.logCmd(cmdsender, s);
					}

				}
				catch (Exception ex)
				{
					throw new WrongUsageException("pvputils.command.realPlayer");
				}
			}
		}
	}
}
