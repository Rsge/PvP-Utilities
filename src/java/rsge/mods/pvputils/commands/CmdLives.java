package rsge.mods.pvputils.commands;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.main.Logger;


/**
 * Lives base command
 * 
 * @author Rsge
 */
public class CmdLives extends CmdBase {
	public CmdLives() {
		super("lives", "enable", "disable", "reset", "add", "remove");
		permissionLevel = 0;
	}

	/* ————————————————————————————————————————————————————— */

	// Overrides
	@Override
	public boolean isVisible(ICommandSender cmdsender) {
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender cmdsender, String[] args) {
		if (isCmdsAllowed(cmdsender))
			return super.addTabCompletionOptions(cmdsender, args);
		else
			return null;
	}

	@Override
	public int[] getSyntaxOptions(ICommandSender cmdsender) {
		return isCmdsAllowed(cmdsender) ? new int[] {0, 1, 2, 3} : super.getSyntaxOptions(cmdsender);
	}

	/* ————————————————————————————————————————————————————— */

	@Override
	public void handleCommand(ICommandSender cmdsender, String[] args) {
		String s;
		String max = "You reached the max amount of lives.";
		EntityPlayerMP p;

		// With no extra argument
		// /pvputils lives
		if (args.length == 0 && cmdsender instanceof EntityPlayer){
			p = (EntityPlayerMP) cmdsender;
			Lives.chatLives(p);
		}

		// With 1 argument
		// /pvputils lives <enable/disable/reset/add/remove/playername/uuid>
		else if (args.length == 1 && cmdsender instanceof EntityPlayer){
			p = (EntityPlayerMP) cmdsender;

			switch (args[0].toLowerCase()) {
			// /pvputils lives enable
			case "enable":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				try{
					Lives.init();
					Config.livesEnabled = true;
				}
				catch (IOException e){
					throw new CommandException("pvputils.command.ioException");
				}

				s = "Enabled lives.";
				sendChat(cmdsender, s);
				Logger.logCmd(cmdsender, s);
				break;

			// /pvputils lives disable
			case "disable":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				if (!Lives.stop())
					throw new CommandException("pvputils.command.ioException");

				Config.livesEnabled = false;

				s = "Disabled lives";
				sendChat(cmdsender, s);
				Logger.logCmd(cmdsender, s);
				break;

			// /pvputils lives reset
			case "reset":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Lives.resetLives(p);

				sendChat(cmdsender, "Reset your lives.");
				Logger.logCmd(cmdsender, "Reset his/her lives.");
				break;

			// /pvputils lives add
			case "add":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Lives.addLives(p, 1);

				if (!Lives.tooManyAdded){
					s = "Added 1 life to ";
					sendChat(cmdsender, s + "yourself.");
					Logger.logCmd(cmdsender, s + "him-/herself.");
				}
				else{
					sendChat(cmdsender, max);
					Logger.logCmd(cmdsender, "Tried adding life, reached max amount.");
					Lives.tooManyAdded = false;
				}
				break;

			// "pvputils lives remove
			case "remove":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Lives.removeLives(p, 1);

				s = "Removed 1 life from ";
				if (!Lives.playerBanned)
					sendChat(cmdsender, s + "yourself.");
				else
					Lives.playerBanned = false;
				Logger.logCmd(cmdsender, s + "him-/herself.");
				break;

			// /pvputils lives [playername/uuid]
			default:
				// /pvputils lives [uuid]
				try{
					UUID u = UUID.fromString(args[0]);
					byte l = Lives.getLives(u);

					String end;
					if (l == 1)
						end = " life left.";
					else
						end = " lives left.";

					IChatComponent msgStart = new ChatComponentText("This guy has ");
					IChatComponent msgLives = Lives.formatLives(l);
					IChatComponent msgEnd = new ChatComponentText(end);

					IChatComponent msgFinal = msgStart.appendSibling(msgLives).appendSibling(msgEnd);
					msgFinal.setChatStyle(new ChatStyle().setBold(true));
					cmdsender.addChatMessage(msgFinal);
				}
				catch (IllegalArgumentException ex){
					// /pvputils lives [playername]
					try{
						String name = args[0];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						EntityPlayer receiver = (EntityPlayer) cmdsender;
						Lives.chatLivesTo(p, receiver);
					}
					catch (NullPointerException e){
						throw new SyntaxErrorException("pvputils.command.realPlayer");
					}
				}
				break;
			}
		}

		// With 2 arguments
		// /pvputils lives [reset/set/add/remove] [playername/uuid/all/amount]
		else if (args.length == 2 && cmdsender instanceof EntityPlayer){
			p = (EntityPlayerMP) cmdsender;

			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			// /pvputils lives [set/add/remove] [amount]
			if (StringUtils.isNumeric(args[1])){
				int a = Integer.parseInt(args[1]);

				if (a < 0)
					throw new WrongUsageException("pvputils.command.positiveNumbers");

				switch (args[0].toLowerCase()) {
				// /pvputils lives set [amount]
				case "set":
					Lives.setLives(p, a);

					if (!Lives.tooManyAdded){
						sendChat(cmdsender, "Set your lives to " + a + ".");
						Logger.logCmd(cmdsender, "Set his/her lives to " + a + ".");
					}
					else{
						sendChat(cmdsender, max);
						Logger.logCmd(cmdsender, "Set his/her lives to max.");
						Lives.tooManyAdded = false;
					}
					break;

				// /pvputils lives add [amount]
				case "add":
					Lives.addLives(p, a);

					if (!Lives.tooManyAdded){
						s = "Added " + a + " lives to ";
						sendChat(cmdsender, s + "yourself.");
						Logger.logCmd(cmdsender, s + "him-/herself.");
					}
					else{
						sendChat(cmdsender, max);
						Logger.logCmd(cmdsender, "Tried adding " + a + " lives to him-/herself, reached max.");
						Lives.tooManyAdded = false;
					}
					break;

				// /pvputils lives remove [amount]
				case "remove":
					Lives.removeLives(p, a);

					if (!Lives.playerBanned){
						s = "Removed " + a + " lives from ";
						sendChat(cmdsender, s + "yourself.");
						Logger.logCmd(cmdsender, s + "him-/herself.");
					}
					else{
						Logger.logCmd(cmdsender, "Removed all his/her remaining lives.");
						Lives.playerBanned = false;
					}
					break;
				}
			}
			// /pvputils lives [reset/add/remove] all
			else if (args[1].equalsIgnoreCase("all")){
				switch (args[0].toLowerCase()) {
				// /pvputils lives reset all
				case "reset":
					Lives.resetAllLives();

					s = "Reset all lives.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					break;

				// /pvputils lives add all
				case "add":
					Lives.addLivesToAll(1);

					s = "Added 1 life to everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Lives.tooManyAdded){
						sendChat(cmdsender, "In the process someone reached the max amount of lives.");
						Lives.tooManyAdded = false;
					}
					break;

				// /pvputils lives remove all
				case "remove":
					Lives.removeLivesFromAll(1);

					s = "Removed 1 life from everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Lives.playerBanned){
						sendChat(cmdsender, "In the process someone ran out of lives and was banned.");
						Lives.playerBanned = false;
					}
					break;
				}
			}
			// /pvputils lives [reset/add/remove] [playername/uuid]
			else{
				// /pvputils lives [reset/add/remove] [uuid]
				try{
					UUID u = UUID.fromString(args[1]);

					switch (args[0].toLowerCase()) {
					// /pvputils lives reset [uuid]
					case "reset":
						Lives.resetLives(u);

						sendChat(cmdsender, "Reset this guy's lives.");
						Logger.logCmd(cmdsender, "Reset lives of " + u.toString() + ".");
						break;

					// /pvputils lives add [uuid]
					case "add":
						Lives.addLives(u, 1);

						if (!Lives.tooManyAdded){
							s = "Added 1 life to ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							sendChat(cmdsender, "This guy reached the max amount of lives.");
							Logger.logCmd(cmdsender, "Tried adding 1 life to " + u.toString() + ", reached max.");
							Lives.tooManyAdded = false;
						}
						break;

					// /pvputils lives remove [uuid]
					case "remove":
						Lives.removeLives(u, 1);

						if (!Lives.playerBanned){
							s = "Removed 1 life from ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							s = "Removed remaining life from ";
							sendChat(cmdsender, s + "this guy. Player banned!");
							Logger.logCmd(cmdsender, s + u.toString() + ". Player banned.");
							Lives.playerBanned = false;
						}
						break;
					}
				}
				catch (IllegalArgumentException ex){
					// /pvputils lives [reset/add/remove] [playername]
					try{
						String name = args[1];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);

						switch (args[0].toLowerCase()) {
						// /pvputils lives reset [playername]
						case "reset":
							Lives.resetLives(p);

							s = "Reset " + name + "'s lives.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
							break;

						// /pvputils lives add [playername]
						case "add":
							Lives.addLives(p, 1);

							if (!Lives.tooManyAdded){
								s = "Added 1 life to " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								sendChat(cmdsender, name + " reached the max amount of lives.");
								Logger.logCmd(cmdsender, "Tried adding 1 life to " + name + ", reached max.");
								Lives.tooManyAdded = false;
							}
							break;

						// /pvputils lives remove [playername]
						case "remove":
							Lives.removeLives(p, 1);

							if (!Lives.playerBanned){
								s = "Removed 1 life from " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								s = "Removed remaining life from " + name + ". Player banned!";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
								Lives.playerBanned = false;
							}
							break;
						}
					}
					catch (NullPointerException e){
						throw new SyntaxErrorException("pvputils.command.realPlayer");
					}
				}
			}
		}

		// With all 3 arguments
		// /pvputils lives [set/add/remove] [playername/uuid/all] [amount]
		else if (args.length == 3 && cmdsender instanceof EntityPlayer){
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			int a = 0;
			try{
				a = Integer.parseInt(args[2]);
			}
			catch (NumberFormatException ex){
				throw new SyntaxErrorException("pvputils.command.notFound");
			}
			if (a < 0)
				throw new WrongUsageException("pvputils.command.positiveNumbers");

			// /pvputils lives [set/add/remove] all [amount]
			if (args[1].equalsIgnoreCase("all")){
				switch (args[0].toLowerCase()) {
				// /pvputils lives set all [amount]
				case "set":
					Lives.setAllLives(a);

					if (!Lives.tooManyAdded){
						s = "Set everyone's lives to " + a + ".";
						sendChat(cmdsender, s);
						Logger.logCmd(cmdsender, s);
					}
					else{
						sendChat(cmdsender, "Everyone now has the max amount of lives.");
						Logger.logCmd(cmdsender, "Set everyone's lives to max.");
						Lives.tooManyAdded = false;
					}
					break;

				// /pvputils lives add all [amount]
				case "add":
					Lives.addLivesToAll(a);

					s = "Added " + a + " lives to everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Lives.tooManyAdded){
						sendChat(cmdsender, "In the process someone reached the max amount of lives.");
						Lives.tooManyAdded = false;
					}
					break;

				// /pvputils lives remove all [amount]
				case "remove":
					Lives.removeLivesFromAll(a);

					s = "Removed " + a + " lives from everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Lives.playerBanned){
						sendChat(cmdsender, "In the process someone lost his/her last live . Player(s) banned!");
						Lives.playerBanned = false;
					}
					break;
				}
			}
			// /pvputils lives [set/add/remove] [playername/uuid] [amount]
			else{
				// /pvputils lives [set/add/remove] [uuid] [amount]
				try{
					UUID u = UUID.fromString(args[1]);

					switch (args[0].toLowerCase()) {
					// /pvputils lives set [uuid] [amount]
					case "set":
						Lives.setLives(u, a);

						if (!Lives.tooManyAdded){
							sendChat(cmdsender, "Set this guy's lives to " + a + ".");
							Logger.logCmd(cmdsender, "Set lives of " + u.toString() + " to " + a + ".");
						}
						else{
							sendChat(cmdsender, "This guy reached the max amount of lives.");
							Logger.logCmd(cmdsender, "Set lives of " + u.toString() + " to max.");
							Lives.tooManyAdded = false;
						}
						break;

					// /pvputils lives add [uuid] [amount]
					case "add":
						Lives.addLives(u, a);

						if (!Lives.tooManyAdded){
							s = "Added " + a + " lives to ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							sendChat(cmdsender, "This guy reached the max amount of lives.");
							Logger.logCmd(cmdsender, "Tried adding " + a + " lives to " + u.toString() + ", reached max.");
							Lives.tooManyAdded = false;
						}
						break;

					// /pvputils lives remove [playername] [amount]
					case "remove":
						Lives.removeLives(u, a);

						if (!Lives.playerBanned){
							s = "Removed " + a + " lives from";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							s = "Removed remaining lives from ";
							sendChat(cmdsender, s + "this guy. Player banned!");
							Logger.logCmd(cmdsender, s + u.toString() + ". Player banned!");
							Lives.playerBanned = false;
						}
						break;
					}
				}
				catch (IllegalArgumentException ex){
					try{
						String name = args[1];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);

						switch (args[0].toLowerCase()) {
						// /pvputils lives set [playername] [amount]
						case "set":
							Lives.setLives(p, a);

							if (!Lives.tooManyAdded){
								s = "Set " + name + "'s lives to " + a + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								sendChat(cmdsender, name + " reached the max amount of lives.");
								Logger.logCmd(cmdsender, "Set " + name + "'s lives to max.");
								Lives.tooManyAdded = false;
							}
							break;

						// /pvputils lives add [playername] [amount]
						case "add":
							Lives.addLives(p, a);

							if (!Lives.tooManyAdded){
								s = "Added " + a + " lives to " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								sendChat(cmdsender, name + " reached the max amount of lives.");
								Logger.logCmd(cmdsender, "Tried adding " + a + " lives to " + name + ", reached max.");
								Lives.tooManyAdded = false;
							}
							break;

						// /pvputils lives remove [playername] [amount]
						case "remove":
							Lives.removeLives(p, a);

							if (!Lives.playerBanned){
								s = "Removed " + a + " lives from " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								s = "Removed remaining lives from " + name + ". Player banned!";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
								Lives.playerBanned = false;
							}
							break;
						}
					}
					catch (NullPointerException e){
						throw new SyntaxErrorException("pvputils.command.realPlayer");
					}
				}
			}
		}
	}
}
