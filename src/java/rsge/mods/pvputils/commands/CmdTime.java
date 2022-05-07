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
import rsge.mods.pvputils.data.Time;
import rsge.mods.pvputils.main.Logger;


/**
 * Time base command
 * 
 * @author Rsge
 */
public class CmdTime extends CmdBase {
	public CmdTime() {
		super("time", "enable", "disable", "start", "stop", "reset", "add", "remove");
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
		String max = "You reached the max possible time.";
		EntityPlayerMP p;

		// With no extra arguments
		// /pvputils time
		if (args.length == 0 && cmdsender instanceof EntityPlayer){
			p = (EntityPlayerMP) cmdsender;
			Time.chatTime(p);
		}

		// With 1 argument
		// /pvputils time <enable/disable/start/stop/reset/add/remove/playername/uuid>
		else if (args.length == 1 && cmdsender instanceof EntityPlayer){
			p = (EntityPlayerMP) cmdsender;

			switch (args[0].toLowerCase()) {
			// /pvputils time enable
			case "enable":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				try{
					Time.init();
					Config.timeEnabled = true;
				}
				catch (IOException e){
					throw new CommandException("pvputils.command.ioException");
				}

				s = "Enabled time.";
				sendChat(cmdsender, s);
				Logger.logCmd(cmdsender, s);
				break;

			// /pvputils time disable
			case "disable":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				if (!Time.stop())
					throw new CommandException("pvputils.command.noPermission");

				Config.timeEnabled = false;

				s = "Disabled time";
				sendChat(cmdsender, s);
				Logger.logCmd(cmdsender, s);
				break;

			// /pvputils time start
			case "start":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Time.startTime(p);

				sendChat(cmdsender, "Started your time.");
				Logger.logCmd(cmdsender, "Started his/her time.");
				break;

			// /pvputils time stop
			case "stop":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Time.stopTime(p);

				sendChat(cmdsender, "Stopped your time.");
				Logger.logCmd(cmdsender, "Stopped his/her time.");
				break;

			// /pvputils time reset
			case "reset":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Time.resetTime(p);

				sendChat(cmdsender, "Reset your time.");
				Logger.logCmd(cmdsender, "Reset his/her time.");
				break;

			// /pvputils time add
			case "add":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Time.addTime(p, Config.addedTime);

				if (!Time.tooMuchAdded){
					s = "Added " + Config.addedTime / 60 + " minutes to ";
					sendChat(cmdsender, s + "yourself.");
					Logger.logCmd(cmdsender, s + "him-/herself.");
				}
				else{
					sendChat(cmdsender, max);
					Logger.logCmd(cmdsender, "Tried adding " + Config.addedTime / 60 + " minutes, reached max amount.");
					Time.tooMuchAdded = false;
				}
				break;

			// /pvputils time remove
			case "remove":
				if (!isCmdsAllowed(cmdsender))
					throw new CommandException("pvputils.command.noPermission");

				Time.removeTime(p, Config.addedTime);

				s = "Removed " + Config.addedTime / 60 + " minutes from ";
				if (!Time.playerOutOfTime)
					sendChat(cmdsender, s + "yourself.");
				else
					Time.playerOutOfTime = false;
				Logger.logCmd(cmdsender, s + "him-/herself.");
				break;

			// /pvputils time [playername/uuid]
			default:
				// /pvputils time [uuid]
				try{
					UUID u = UUID.fromString(args[0]);
					long t = Time.getTime(u);

					IChatComponent msgStart = new ChatComponentText("This guy has ");
					IChatComponent msgTime = Time.formatTime(t);
					IChatComponent msgEnd = new ChatComponentText(" left.");

					IChatComponent msgFinal = msgStart.appendSibling(msgTime).appendSibling(msgEnd);
					msgFinal.setChatStyle(new ChatStyle().setBold(true));
					cmdsender.addChatMessage(msgFinal);
				}
				catch (IllegalArgumentException ex){
					try{
						String name = args[0];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
						EntityPlayer receiver = (EntityPlayer) cmdsender;
						Time.chatTimeTo(p, receiver);
					}
					catch (Exception exc){
						throw new SyntaxErrorException("pvputils.command.realPlayer");
					}
				}
				break;
			}
		}

		// With 2 arguments
		// /pvputils time [start/stop/reset/set/add/remove] [playername/uuid/all/amount]
		else if (args.length == 2 && cmdsender instanceof EntityPlayer){
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			// /pvputils time [set/add/remove] [amount]
			if (StringUtils.isNumeric(args[1])){
				int a = Integer.parseInt(args[1]) * 60;
				if (a < 0)
					throw new WrongUsageException("pvputils.command.positiveNumbers");
				p = (EntityPlayerMP) cmdsender;

				switch (args[0].toLowerCase()) {
				// /pvputils time set [amount]
				case "set":
					Time.setTime(p, a);

					if (Time.tooMuchAdded){
						sendChat(cmdsender, max);
						Logger.logCmd(cmdsender, "Set his/her time to max.");
						Time.tooMuchAdded = false;
					}
					else if (Time.playerOutOfTime){
						Logger.logCmd(cmdsender, "Removed his/her remaining time.");
						Time.playerOutOfTime = false;
					}
					else{
						sendChat(cmdsender, "Set your time to " + a / 60 + " minutes.");
						Logger.logCmd(cmdsender, "Set his/her time to " + a / 60 + " minutes.");
					}
					break;

				// /pvputils time add [amount]
				case "add":
					Time.addTime(p, a);

					if (!Time.tooMuchAdded){
						s = "Added " + a / 60 + " minutes to ";
						sendChat(cmdsender, s + "yourself.");
						Logger.logCmd(cmdsender, s + "him-/herself.");
					}
					else{
						sendChat(cmdsender, max);
						Logger.logCmd(cmdsender, "Tried adding " + a + " minutes him-/herself, reached max.");
						Time.tooMuchAdded = false;
					}
					break;

				// /pvputils time remove [amount]
				case "remove":
					Time.removeTime(p, a);

					if (!Time.playerOutOfTime){
						s = "Removed " + a / 60 + " minutes from ";
						sendChat(cmdsender, "yourself.");
						Logger.logCmd(cmdsender, s + "him-/herself.");
					}
					else{
						Logger.logCmd(cmdsender, "Removed his/her remaining time.");
						Time.playerOutOfTime = false;
					}
					break;
				}
			}
			// /pvputils time [start/stop/reset/add/remove] [playername/uuid/all]
			else if (args[1].equalsIgnoreCase("all")){
				switch (args[0].toLowerCase()) {
				// /pvputils time reset all
				case "reset":
					Time.resetAllTime();

					s = "Reset time of all players";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					break;

				// /pvputils time add all
				case "add":
					Time.addTimeToAll(Config.addedTime);

					s = "Added " + Config.addedTime / 60 + " minutes to everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Time.tooMuchAdded){
						sendChat(cmdsender, "In the process someone reached the max possible minutes.");
						Time.tooMuchAdded = false;
					}
					break;

				// /pvputils time remove all
				case "remove":
					Time.removeTimeFromAll(Config.addedTime);

					s = "Removed " + Config.addedTime / 60 + " minutes from everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Time.playerOutOfTime){
						sendChat(cmdsender, "In the process someone ran out of time.");
						Time.playerOutOfTime = false;
					}
					break;
				}
			}
			// / pvputils time [start/stop/reset/add/remove] [playername/uuid]
			else{
				// /pvputils time [reset/add/remove] [uuid]
				try{
					UUID u = UUID.fromString(args[1]);

					switch (args[0].toLowerCase()) {
					// /pvputils time reset [uuid]
					case "reset":
						Time.resetTime(u);

						sendChat(cmdsender, "Reset this guy's time.");
						Logger.logCmd(cmdsender, "Reset time of " + u.toString() + ".");
						break;

					// /pvputils time add [uuid]
					case "add":
						Time.addTime(u, Config.addedTime);

						if (!Time.tooMuchAdded){
							s = "Added " + Config.addedTime / 60 + " minutes to ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							sendChat(cmdsender, "This guy reached the max possible minutes.");
							Logger.logCmd(cmdsender, "Tried adding " + Config.addedTime / 60 + " minutes to " + u.toString() + ", reached max.");
							Time.tooMuchAdded = false;
						}
						break;

					// /pvputils time remove [uuid]
					case "remove":
						Time.removeTime(u, Config.addedTime);

						if (!Time.playerOutOfTime){
							s = "Removed " + Config.addedTime + " minutes from ";
							sendChat(cmdsender, s + " this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							s = "Removed remaining time from ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
							Time.playerOutOfTime = false;
						}
						break;
					}
				}
				catch (IllegalArgumentException ex){
					// /pvputils time [start/stop/reset/add/remove] [playername]
					try{
						String name = args[1];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);

						switch (args[0].toLowerCase()) {
						// /pvputils time start [playername]
						case "start":
							Time.startTime(p);

							s = "Started " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
							break;

						// /pvputils time stop [playername]
						case "stop":
							Time.stopTime(p);

							s = "Stopped " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
							break;

						// /pvputils time reset [playername]
						case "reset":
							Time.resetTime(p);

							s = "Reset " + name + "'s time.";
							sendChat(cmdsender, s);
							Logger.logCmd(cmdsender, s);
							break;

						// /pvputils time add [playername]
						case "add":
							Time.addTime(p, Config.addedTime);

							if (!Time.tooMuchAdded){
								s = "Added " + Config.addedTime / 60 + " minutes to " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								sendChat(cmdsender, name + " reached the max possible time.");
								Logger.logCmd(cmdsender, "Tried adding " + Config.addedTime / 60 + " minutes to " + name + ", reached max.");
								Time.tooMuchAdded = false;
							}
							break;

						// /pvputils time remove [playername]
						case "remove":
							Time.removeTime(p, Config.addedTime);

							if (!Time.playerOutOfTime){
								s = "Removed " + Config.addedTime / 60 + " minutes from " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								s = "Removed remaining time from " + name + ".";
								sendChat(cmdsender, "Removed remaining time from " + name + ". Player kicked!");
								Logger.logCmd(cmdsender, s);
								Time.playerOutOfTime = false;
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

		// With 3 arguments
		// /pvputils time [set/add/remove] [playername/uuid/all/multiplier] [amount/percentage]
		else if (args.length == 3 && cmdsender instanceof EntityPlayer){
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			int a = 0;
			try{
				a = Integer.parseInt(args[2]);
				if (a < 0)
					throw new WrongUsageException("pvputils.command.positiveNumbers");
			}
			catch (NumberFormatException ex){
				throw new SyntaxErrorException("pvputils.command.notFound");
			}

			// /pvputils time set multiplier [percentage]
			if (args[1].equalsIgnoreCase("multiplier")){
				if (args[0].equalsIgnoreCase("set")){
					if (a < 0){
						p = (EntityPlayerMP) cmdsender;
						float m = (float) a / 100.0f;
						Time.setTimeMultiplier(p, m);
					}
					else
						throw new WrongUsageException("pvputils.command.positiveNumbers");
				}
				else
					throw new SyntaxErrorException("pvputils.command.multiplierSet");
			}
			else
				a *= 60;

			// /pvputils time [set/add/remove] all [amount]
			if (args[1].equalsIgnoreCase("all")){
				switch (args[0].toLowerCase()) {
				// /pvputils set all [amount]
				case "set":
					if (Lives.tooManyAdded){
						Time.setAllTime(a);

						s = "Set everyone's time to " + a / 60 + "minutes.";
						sendChat(cmdsender, s);
						Logger.logCmd(cmdsender, s);
					}
					else{
						sendChat(cmdsender, "Everyone has the max possible time.");
						Logger.logCmd(cmdsender, "Set everyone's time to max.");
						Time.tooMuchAdded = false;
					}
					break;

				// /pvputils add all [amount]
				case "add":
					Time.addTimeToAll(a);

					s = "Added " + Config.addedTime / 60 + " minutes to everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Time.tooMuchAdded){
						sendChat(cmdsender, "In the process someone reached the max possible minutes.");
						Time.tooMuchAdded = false;
					}
					break;

				// /pvputils remove all [amount]
				case "remove":
					Time.removeTimeFromAll(a);

					s = "Removed " + a / 60 + " minutes from everyone.";
					sendChat(cmdsender, s);
					Logger.logCmd(cmdsender, s);
					if (Time.playerOutOfTime){
						sendChat(cmdsender, "In the process someone lost his/her last remaining time.");
						Time.playerOutOfTime = false;
					}
					break;
				}
			}
			// /pvputils time [set/add/remove] [playername/uuid] [amount]
			else{
				// /pvputils time [set/add/remove] [uuid] [amount]
				try{
					UUID u = UUID.fromString(args[1]);

					switch (args[0].toLowerCase()) {
					// /pvputils time set [uuid] [amount]
					case "set":
						Time.setTime(u, a);

						if (Time.tooMuchAdded){
							sendChat(cmdsender, "This guy reached the max possible time.");
							Logger.logCmd(cmdsender, "Set time of " + u.toString() + " to max.");
							Time.tooMuchAdded = false;
						}
						else if (Time.playerOutOfTime){
							Logger.logCmd(cmdsender, "Removed this guy's remaining time.");
							Time.playerOutOfTime = false;
						}
						else{
							sendChat(cmdsender, "Set this guy's time to " + a / 60 + " minutes.");
							Logger.logCmd(cmdsender, "Set time of " + u.toString() + " to " + a / 60 + " minutes.");
						}
						break;

					// /pvputils time add [uuid] [amount]
					case "add":
						Time.addTime(u, a);

						if (!Time.tooMuchAdded){
							s = "Added " + a / 60 + " minutes to ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							sendChat(cmdsender, "This guy reached the max possible time.");
							Logger.logCmd(cmdsender, "Tried adding " + a / 60 + " minutes to " + u.toString() + ", reached max.");
							Time.tooMuchAdded = false;
						}
						break;

					// /pvputils time remove [uuid] [amount]
					case "remove":
						Time.removeTime(u, a);

						if (!Time.playerOutOfTime){
							s = "Removed " + a / 60 + " minutes from ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						else{
							s = "Removed remaining time from ";
							sendChat(cmdsender, s + "this guy.");
							Logger.logCmd(cmdsender, s + u.toString() + ".");
						}
						break;
					}
				}
				catch (IllegalArgumentException ex){
					try{
						String name = args[1];
						p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);

						switch (args[0].toLowerCase()) {
						// /pvputils time set [playername] [amount]
						case "set":
							Time.setTime(p, a);

							if (Time.tooMuchAdded){
								s = "Set " + name + "'s time to " + a / 60 + " minutes.";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else if (Time.playerOutOfTime){
								s = "Removed remaining time from " + name + ". Player kicked!";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
								Time.playerOutOfTime = false;
							}
							else{
								sendChat(cmdsender, name + " reached the max possible time.");
								Logger.logCmd(cmdsender, "Set " + name + "'s time to max.");
								Time.tooMuchAdded = false;
							}
							break;

						// /pvputils time add [playername] [amount]
						case "add":
							Time.addTime(p, a);

							if (!Time.tooMuchAdded){
								s = "Added " + a / 60 + " minutes to " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								sendChat(cmdsender, name + " reached the max possible time.");
								Logger.logCmd(cmdsender, "Tried adding " + a / 60 + " minutes to " + name + ", reached max.");
								Time.tooMuchAdded = false;
							}
							break;

						// /pvputils time remove [playername] [amount]
						case "remove":
							Time.removeTime(p, a);

							if (!Time.playerOutOfTime){
								s = "Removed " + a / 60 + " minutes from " + name + ".";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
							}
							else{
								s = "Removed remaining time from " + name + ". Player kicked!";
								sendChat(cmdsender, s);
								Logger.logCmd(cmdsender, s);
								Time.playerOutOfTime = false;
							}
							Logger.logCmd(cmdsender, s);
							break;
						}
					}
					catch (NullPointerException e){
						throw new SyntaxErrorException("pvputils.command.realPlayer");
					}
				}
			}
		}
		// With all 4 arguments
		// /pvputils time set multiplier [playername/uuid/all] [percentage]
		else if (args.length == 4 && cmdsender instanceof EntityPlayer){
			if (!isCmdsAllowed(cmdsender))
				throw new CommandException("pvputils.command.noPermission");

			if (args[1].equalsIgnoreCase("multiplier")){
				if (args[0].equalsIgnoreCase("set")){
					int a = 1;
					try{
						a = Integer.parseInt(args[3]);
						if (a < 0)
							throw new WrongUsageException("pvputils.command.positiveNumbers");
					}
					catch (NumberFormatException ex){
						throw new SyntaxErrorException("pvputils.command.notFound");
					}
					float m = (float) a / 100.0f;

					// /pvputils time set multiplier all [percentage]
					if (args[2].equalsIgnoreCase("all")){
						Time.setAllTimeMultipliers(m);
					}
					else{
						// /pvputils time set multiplier [uuid] [percentage]
						try{
							UUID u = UUID.fromString(args[2]);
							Time.setTimeMultiplier(u, m);
						}
						catch (IllegalArgumentException ex){
							// /pvputils time set multiplier [playername] [percentage]
							try{
								String name = args[2];
								p = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
								Time.setTimeMultiplier(p, m);
							}
							catch (NullPointerException e){
								throw new SyntaxErrorException("pvputils.command.realPlayer");
							}
						}
					}
				}
				else
					throw new SyntaxErrorException("pvputils.command.multiplierSet");
			}
			else
				throw new SyntaxErrorException("pvputils.command.notFound");
		}
	}
}
