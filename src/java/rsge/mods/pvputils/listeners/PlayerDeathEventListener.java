package rsge.mods.pvputils.listeners;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.data.Lives;
import rsge.mods.pvputils.data.ScoreBoard;
import rsge.mods.pvputils.main.Logger;


/**
 * Lives on death handling
 * 
 * @author Rsge
 */
public class PlayerDeathEventListener
{
	public PlayerDeathEventListener()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Removing life & updating scoreboard accordingly
	 * 
	 * @param e Player death event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(LivingDeathEvent e)
	{
		if (Config.excessiveLogging)
			Logger.info("\"" + e.entityLiving.getCommandSenderName() + "\" died");
		else if (Config.debugLogging && e.entityLiving instanceof EntityPlayerMP)
			Logger.info("Player \"" + e.entityLiving.getCommandSenderName() + "\" died");

		if (e.entityLiving instanceof EntityPlayerMP)
		{
			EntityPlayerMP p = (EntityPlayerMP) e.entityLiving;
			Lives.death(p, e.source);

			if (Config.scoreboardEnabled)
				ScoreBoard.updatePlayer((EntityPlayer) p);
		}
	}

	/**
	 * Chatting new amount of lifes to player
	 * 
	 * @param e Player respawn event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		if (Config.debugLogging)
			Logger.info("Player \"" + e.player.getCommandSenderName() + "\" respawned");

		if (!Config.noLifeChat)
		{
			EntityPlayerMP p = (EntityPlayerMP) e.player;
			Lives.chatLives(p);
		}
	}
}
