package rsge.mods.pvputils.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;


/**
 * Abstract command base
 * 
 * @author Rsge
 */
public abstract class CmdBase implements ISubCmd
{
	private String name;
	private List<String> subCommands = new ArrayList<>();
	protected int permissionLevel = 0;

	/* ————————————————————————————————————————————————————— */

	public CmdBase(String name, String... subCommands)
	{
		this.name = name;
		this.subCommands = Arrays.asList(subCommands);
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Is player an op or owner of singleplayer world
	 * 
	 * @param  user GameProfile of player
	 * @return      if player is op
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static boolean isPlayerOp(GameProfile user)
	{
		// Returns "true" if either
		// "user" is allowed to use commands
		return MinecraftServer.getServer().getConfigurationManager().func_152596_g(user)
				// or is owner of the singleplayer-server
				|| MinecraftServer.getServer().isSinglePlayer() && MinecraftServer.getServer().getServerOwner().equals(user);
	}

	/**
	 * Is player allowed to perform commands
	 * 
	 * @param  cmdsender Command sender
	 * @return           if player is allowed to use op-commands
	 */
	public static boolean isCmdsAllowed(ICommandSender cmdsender)
	{
		// If the command-sender is a player
		if (cmdsender instanceof EntityPlayer)
		{
			EntityPlayer p = (EntityPlayer) cmdsender;
			GameProfile user = p.getGameProfile();
			return isPlayerOp(user);
		}
		else
			return true;
	}

	/* ————————————————————————————————————————————————————— */

	/**
	 * Send chat message to player
	 * 
	 * @param cmdsender Player to send chat to
	 * @param s         String to send
	 */
	protected void sendChat(ICommandSender cmdsender, String s)
	{
		cmdsender.addChatMessage(new ChatComponentText(s));
	}

	/* ————————————————————————————————————————————————————— */

	// Overrides
	/**
	 * @return Name of command
	 */
	@Override
	public String getCommandName()
	{
		return name;
	}

	/**
	 * @return Permission level of command as integer
	 */
	@Override
	public int getPermissionLevel()
	{
		return permissionLevel;
	}

	/**
	 * Adds options to Tab-autocompletion
	 * 
	 * @param  cmdsender Player who presses tab
	 * @param  s         Startstring
	 * @return           Autocompletion options
	 */
	@Override
	public List<String> addTabCompletionOptions(ICommandSender cmdsender, String[] args)
	{
		List<String> res = new ArrayList<>();
		if (args.length == 1)
		{
			// Goes through all "subCommands" and put them in "s" consecutively
			for (String s : subCommands)
			{
				// If the current "subCommand" starts the same as the input "s"
				if (s.startsWith(args[0]))
				{
					// adds it to the list of possible results
					res.add(s);
				}
			}
		}
		// returns the list of results
		return res;
	}

	/**
	 * Is command visible for player in autocompletion
	 * 
	 * @param  cmdsender Player who tries to autocomplete
	 * @return           if sender can see command
	 */
	@Override
	public boolean isVisible(ICommandSender cmdsender)
	{
		return getPermissionLevel() <= 0 || isCmdsAllowed(cmdsender);
	}

	/**
	 * Get syntax options of command
	 * 
	 * @param  cmdsender Player using command
	 * @return           Syntax options for command
	 */
	@Override
	public int[] getSyntaxOptions(ICommandSender cmdsender)
	{
		return new int[] {0};
	}
}
