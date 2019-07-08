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
import rsge.mods.pvputils.main.Logger;


/**
 * Lives base command
 * 
 * @author Rsge
 */
public class CmdLives extends CmdBase
{
	public CmdLives()
	{
		super("lives", "enable", "disable", "reset", "add", "remove");
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
		// With no extra argument
		// /pvputils lives
		if (args.length == 0 && cmdsender instanceof EntityPlayer)
		{
			EntityPlayerMP p = (EntityPlayerMP) cmdsender;
			Lives.chatLives(p);
		}

		// With 1 argument
		// /pvputils lives <reset/add/remove/uuid/playername>
		else if (args.length == 1 && cmdsender instanceof EntityPlayer)
		{
			if (args[0].equalsIgnoreCase("enable"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				else
				{
					Config.livesEnabled = true;

					// Log command
					String s = "Enabled lives.";
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
					Config.livesEnabled = false;

					// Log command
					String s = "Disabled lives";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
				}
			}

			if (args[0].equalsIgnoreCase("reset"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				EntityPlayerMP p = (EntityPlayerMP) cmdsender;
				Lives.resetLives(p);

				// Log command
				String s = "Reset his/her lives.";
				if (!Lives.tooManyAdded)
					sendChat(cmdsender, "Reset your lives.");
				else
				{
					sendChat(cmdsender, "You reached the max number of lives.");
					Lives.tooManyAdded = false;
				}
				Logger.logCmd(cmdsender, s);
			}
			else if (args[0].equalsIgnoreCase("add"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				EntityPlayerMP p = (EntityPlayerMP) cmdsender;
				Lives.addLife(p);

				// Log command
				String s = "Added 1 life to him-/herself.";
				if (!Lives.tooManyAdded)
					sendChat(cmdsender, "Added 1 life to yourself.");
				else
				{
					sendChat(cmdsender, "You reached the max number of lives.");
					Lives.tooManyAdded = false;
				}
				Logger.logCmd(cmdsender, s);
			}
			else if (args[0].equalsIgnoreCase("remove"))
			{
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				EntityPlayerMP p = (EntityPlayerMP) cmdsender;
				Lives.removeLife(p);

				// Log command
				String s = "Removed 1 life from him-/herself.";
				sendChat(cmdsender, "Removed 1 life from yourself.");
				Logger.logCmd(cmdsender, s);
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
						Lives.chatLivesTo(p, receiver, false);
					}
					catch (Exception exc)
					{
						throw new WrongUsageException("pvputils.command.realPlayer");
					}
				}
			}
		}

		// With 2 arguments
		// /pvputils lives [reset/set/add/remove] [playername/amont]
		else if (args.length == 2 && cmdsender instanceof EntityPlayer)
		{
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");
			try
			{
				// /pvputils lives [set/add/remove] [amont]
				if (StringUtils.isNumeric(args[1]))
				{
					a = Integer.parseInt(args[1]);

					if (a < 0)
						throw new WrongUsageException("pvputils.command.positiveNumbers");

					if (args[0].equalsIgnoreCase("set"))
					{
						EntityPlayerMP p = (EntityPlayerMP) cmdsender;
						Lives.setLives(p, a);

						// Log command
						String s = "Set his/her lives to " + a + ".";
						if (!Lives.tooManyAdded)
							sendChat(cmdsender, "Set your lives to " + a + ".");
						else
						{
							sendChat(cmdsender, "You reached the max number of lives.");
							Lives.tooManyAdded = false;
						}
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("add"))
					{
						EntityPlayerMP p = (EntityPlayerMP) cmdsender;
						Lives.addLives(p, a);

						// Log command
						String s = "Added " + a + " life(s) to him-/herself.";
						if (!Lives.tooManyAdded)
							sendChat(cmdsender, "Added " + a + " life(s) to yourself.");
						else
						{
							sendChat(cmdsender, "You reached the max number of lives.");
							Lives.tooManyAdded = false;
						}
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("remove"))
					{
						EntityPlayerMP p = (EntityPlayerMP) cmdsender;
						Lives.removeLives(p, a);

						// Log command
						String s = "Removed " + a + " life(s) from him-/herself.";
						sendChat(cmdsender, "Removed " + a + " life(s) from yourself.");
						Logger.logCmd(cmdsender, s);
					}
				}
				// /pvputils lives [reset/add/remove] [playername]
				else
				{
					String name = args[1];

					if (args[0].equalsIgnoreCase("reset"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Lives.resetLives(p);

						// Log command
						String s = "Reset " + name + "'s lives.";
						sendChat(cmdsender, s);
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("add"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Lives.addLife(p);

						// Log command
						String s = "Added 1 life to " + name + ".";
						if (!Lives.tooManyAdded)
							sendChat(cmdsender, s);
						else
						{
							sendChat(cmdsender, name + " has reached the max number of lives.");
							Lives.tooManyAdded = false;
						}
						Logger.logCmd(cmdsender, s);
					}
					else if (args[0].equalsIgnoreCase("remove"))
					{
						EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						Lives.removeLife(p);

						// Log command
						String s = "Removed 1 life from " + name + ".";
						if (!Lives.playerBanned)
							sendChat(cmdsender, s);
						else
						{
							sendChat(cmdsender, "Removed remaining lives from " + name + ". Player banned!");
							Lives.playerBanned = false;
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

		// With all arguments
		// /pvputils lives [set/add/remove] [playername] [amont]
		else if (args.length == 3 && cmdsender instanceof EntityPlayer)
		{
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			try
			{
				String name = args[1];
				a = Integer.parseInt(args[2]);

				if (a < 0)
					throw new WrongUsageException("pvputils.command.positiveNumbers");

				if (args[0].equalsIgnoreCase("set"))
				{
					EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
					Lives.setLives(p, a);

					// Log command
					String s = "Set " + name + "'s lives to " + a + ".";
					if (!Lives.tooManyAdded)
						sendChat(cmdsender, s);
					else
					{
						sendChat(cmdsender, name + " has reached the max number of lives.");
						Lives.tooManyAdded = false;
					}
					Logger.logCmd(cmdsender, s);
				}
				else if (args[0].equalsIgnoreCase("add"))
				{
					EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
					Lives.addLives(p, a);

					// Log command
					String s = "Added " + a + " life(s) to " + name + ".";
					if (!Lives.tooManyAdded)
						sendChat(cmdsender, s);
					else
					{
						sendChat(cmdsender, name + " has reached the max number of lives.");
						Lives.tooManyAdded = false;
					}
					Logger.logCmd(cmdsender, s);
				}
				else if (args[0].equalsIgnoreCase("remove"))
				{
					EntityPlayerMP p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
					Lives.removeLives(p, a);

					// Log command
					String s = "Removed " + a + " life(s) from " + name + ".";
					if (!Lives.playerBanned)
						sendChat(cmdsender, s);
					else
					{
						sendChat(cmdsender, "Removed remaining lives from " + name + ". Player banned!");
						Lives.playerBanned = false;
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
