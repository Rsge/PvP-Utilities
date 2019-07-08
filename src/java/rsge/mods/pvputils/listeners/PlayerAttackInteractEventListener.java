package rsge.mods.pvputils.listeners;

import java.time.Instant;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;


/**
 * Makro denial
 * 
 * @author Rsge
 */
public class PlayerAttackInteractEventListener
{
	public PlayerAttackInteractEventListener()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static HashMap<EntityPlayer, Instant> playerInteractMakro = new HashMap<EntityPlayer, Instant>();
	public static HashMap<EntityPlayer, Instant> playerAttackMakro = new HashMap<EntityPlayer, Instant>();
	public static HashMap<EntityPlayer, Byte> playerMakroTimes = new HashMap<EntityPlayer, Byte>();
	public static HashMap<EntityPlayer, Boolean> playerMakro = new HashMap<EntityPlayer, Boolean>();

	/**
	 * Denying too many interactions per tick
	 * 
	 * @param e Player interact event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		EntityPlayer p = e.entityPlayer;

		if (playerInteractMakro.containsKey(p))
		{
			int makroTime = Instant.now().compareTo(playerInteractMakro.get(p));
			boolean makro = makroTime == 0;
			byte makroTimes;

			if (Config.excessiveLogging)
				Logger.info(makroTime + " " + makro);

			if (playerMakro.containsKey(p))
			{
				if (makro || playerMakro.get(p))
				{
					e.setCanceled(true);
					if (Config.excessiveLogging)
						Logger.info("Interact-Event canceled");

					if (makro)
					{
						String msg = "You are (probably) using makros! Please, just don't :(";
						p.addChatMessage(new ChatComponentText(msg));

						playerMakro.put(p, true);

						if (Config.makroKicker)
						{
							try
							{
								makroTimes = playerMakroTimes.get(p);
								makroTimes += 1;
							}
							catch (Exception ex)
							{
								makroTimes = 0;
							}

							playerMakroTimes.put(p, makroTimes);

							if (playerMakroTimes.get(p) >= 3)
							{
								try
								{
									EntityPlayerMP pl = (EntityPlayerMP) p;
									pl.playerNetServerHandler.kickPlayerFromServer(msg);
									makroTimes = 0;
								}
								catch (Exception ex)
								{
									Logger.error("Error trying to convert player entity");
								}
							}
						}
					}
				}
				else
					playerInteractMakro.remove(p);
			}
			else
				playerMakro.put(p, false);
		}
		else
			playerInteractMakro.put(p, Instant.now());
	}

	/**
	 * Denying too many attacks per tick
	 * 
	 * @param e Player interact event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttack(AttackEntityEvent e)
	{
		EntityPlayer p = e.entityPlayer;

		if (playerInteractMakro.containsKey(p))
		{
			int makroTime = Instant.now().compareTo(playerInteractMakro.get(p));
			boolean makro = makroTime == 0;
			byte makroTimes;

			if (Config.excessiveLogging)
				Logger.info(makroTime + " " + makro);

			if (playerMakro.containsKey(p))
			{
				if (makro || playerMakro.get(p))
				{
					e.setCanceled(true);
					if (Config.excessiveLogging)
						Logger.info("Interact-Event canceled");

					if (makro)
					{
						String msg = "You are (probably) using makros! Please, just don't :(";
						p.addChatMessage(new ChatComponentText(msg));

						playerMakro.put(p, true);

						if (Config.makroKicker)
						{
							try
							{
								makroTimes = playerMakroTimes.get(p);
								makroTimes += 1;
							}
							catch (Exception ex)
							{
								makroTimes = 0;
							}

							playerMakroTimes.put(p, makroTimes);

							if (playerMakroTimes.get(p) >= 3)
							{
								try
								{
									EntityPlayerMP pl = (EntityPlayerMP) p;
									pl.playerNetServerHandler.kickPlayerFromServer(msg);
									makroTimes = 0;
								}
								catch (Exception ex)
								{
									Logger.error("Error trying to convert player entity");
								}
							}
						}
					}
				}
				else
					playerInteractMakro.remove(p);
			}
			else
				playerMakro.put(p, false);
		}
		else
			playerInteractMakro.put(p, Instant.now());
	}
}
