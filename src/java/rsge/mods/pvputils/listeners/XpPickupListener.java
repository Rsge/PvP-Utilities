package rsge.mods.pvputils.listeners;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import rsge.mods.pvputils.config.Config;


/**
 * XP limit enforcer: <br>
 * XP orb pickup stopping
 * 
 * @author Rsge
 */
public class XpPickupListener
{
	public XpPickupListener()
	{
		if (Config.xpLockEnabled)
			MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * XP orb pickup stopping
	 * 
	 * @param e Player XP pickup event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onXpPickup(PlayerPickupXpEvent e)
	{
		if (Config.xpLockEnabled && e.entityPlayer.experienceLevel >= Config.xpLockLevel)
			e.setCanceled(true);
	}
}
