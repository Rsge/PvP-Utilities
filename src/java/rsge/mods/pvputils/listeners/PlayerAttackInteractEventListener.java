package rsge.mods.pvputils.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;


/**
 * Macro denial
 * 
 * @author Rsge
 */
public class PlayerAttackInteractEventListener
{
	public PlayerAttackInteractEventListener()
	{
		if (Config.macroDisable)
			MinecraftForge.EVENT_BUS.register(this);
	}

	private static HashMap<EntityPlayer, Long> playerAttackMacroTime = new HashMap<EntityPlayer, Long>();
	private static HashMap<EntityPlayer, ArrayList<Long>> playerInteractMacroTimes = new HashMap<EntityPlayer, ArrayList<Long>>();
	private static HashMap<EntityPlayer, Byte> playerMacroViolation = new HashMap<EntityPlayer, Byte>();

	/**
	 * Multi-purpose macro denial function
	 * 
	 * @param p Player entity
	 * @param e PlayerInteractEvent || AttackEntityEvent
	 */
	private static void macroDenial(EntityPlayer p, Event e)
	{
		boolean cancelEvent = false;
		ArrayList<Long> interactMacroTimes = new ArrayList<Long>();

		// Filling the HashMaps first
		if (!playerAttackMacroTime.containsKey(p) && e instanceof AttackEntityEvent)
		{
			playerAttackMacroTime.put(p, System.currentTimeMillis());
			return;
		}
		if (!playerInteractMacroTimes.containsKey(p) && e instanceof PlayerInteractEvent)
		{
			interactMacroTimes.add(System.currentTimeMillis());
			playerInteractMacroTimes.put(p, interactMacroTimes);
			return;
		}

		//TODO how to find if clientside
		MinecraftServer mcs = MinecraftServer.getServer();
		if (Config.excessiveLogging)
			Logger.info(e.toString() + " - " + mcs.isSinglePlayer());

		// Attack events are always fired just once per click and are the more important ones to block,
		// so block them instantly when fired too fast after the other.
		if (e instanceof AttackEntityEvent)
		{
			long attackInterval = System.currentTimeMillis() - playerAttackMacroTime.get(p);
			if (Config.excessiveLogging)
				Logger.info("AttackEvent-Interval: " + attackInterval + " from Player " + p.getDisplayName());
			cancelEvent = (attackInterval != 0 && attackInterval <= Config.macroTreshold);
		}
		// Interact events are sometimes fired in strange ways and in strange intervals,
		// so check for multiple short intervals in succession, before counting it as macro.
		else if (e instanceof PlayerInteractEvent)
		{
			ArrayList<Long> interactTimes = new ArrayList<Long>();
			interactTimes = playerInteractMacroTimes.get(p);
			if (interactTimes.size() < 3)
			{
				interactTimes.add(System.currentTimeMillis());
				playerInteractMacroTimes.put(p, interactTimes);
				return;
			}

			for (int i = 1; i < interactTimes.size(); i++)
			{
				long interactInterval = interactTimes.get(i) - interactTimes.get(i - 1);
				if (Config.excessiveLogging && i == interactTimes.size() - 1)
					Logger.info("InteractEvent-Interval: " + interactInterval + " from Player " + p.getDisplayName());
				if (interactInterval > Config.macroTreshold)
					return;
			}
			cancelEvent = true;
		}

		// Canceling event when necessary
		if (cancelEvent)
		{
			e.setCanceled(true);
			if (Config.debugLogging)
				Logger.info("Attack/Interact-Event canceled.");

			String msg = "You are (probably) using macros! Please, just don't :(";
			if (Config.macroKicker)
				msg += " If you don't want to get kicked, anyway >: ]";
			p.addChatMessage(new ChatComponentText(msg));

			// Handling kicking of player if necessary
			if (Config.macroKicker)
			{
				byte macroViolation = 0;

				if (!playerMacroViolation.containsKey(p))
				{
					playerMacroViolation.put(p, macroViolation);
					return;
				}

				macroViolation = playerMacroViolation.get(p);
				macroViolation += 1;

				if (macroViolation >= Config.macroKickerTreshold)
				{
					try
					{
						EntityPlayerMP pmp = (EntityPlayerMP) p;
						pmp.playerNetServerHandler.kickPlayerFromServer(msg);
					}
					catch (ClassCastException ex)
					{
						Logger.error("Error trying to convert player entity for kicking");
					}
					finally
					{
						macroViolation = 0;
						playerMacroViolation.put(p, macroViolation);
					}
				}
				else
					playerMacroViolation.put(p, macroViolation);
			}
		}
		// Updating macro times
		if (e instanceof AttackEntityEvent)
			playerAttackMacroTime.put(p, System.currentTimeMillis());
		else if (e instanceof PlayerInteractEvent)
		{
			interactMacroTimes.remove(0);
			interactMacroTimes.add(System.currentTimeMillis());
			playerInteractMacroTimes.put(p, interactMacroTimes);
		}
	}

	/**
	 * Denying too many attacks per tick
	 * 
	 * @param e Attack entity event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttack(AttackEntityEvent e)
	{
		macroDenial(e.entityPlayer, e);
	}

	/**
	 * Denying too many interactions per tick
	 * 
	 * @param e Player interact event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		macroDenial(e.entityPlayer, e);
	}

	/**
	 * Removes player from relevant maps
	 * 
	 * @param p Player entity
	 */
	public static void logout(EntityPlayer p)
	{
		playerAttackMacroTime.remove(p);
		playerInteractMacroTimes.remove(p);
		playerMacroViolation.remove(p);
	}
}
