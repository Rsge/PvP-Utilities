package rsge.mods.pvputils.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import rsge.mods.pvputils.config.Config;
import rsge.mods.pvputils.main.Logger;


/**
 * Life scoreboard
 * 
 * @author Rsge
 */
public class ScoreBoard {
	public static Scoreboard sb;

	/**
	 * Initializing scoreboard for lives
	 */
	public static void init() {
		sb = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();

		if (sb.getObjective("Lives") != null && sb.getObjective("Lives").getCriteria() != IScoreObjectiveCriteria.field_96641_b){
			sb.func_96519_k(sb.getObjective("Lives"));
			sb.addScoreObjective("Lives", IScoreObjectiveCriteria.field_96641_b);
		}
		else if (sb.getObjective("Lives") == null)
			sb.addScoreObjective("Lives", IScoreObjectiveCriteria.field_96641_b);

		sb.getObjective("Lives").setDisplayName("Lives");
		sb.func_96530_a(Config.scoreboardType, sb.getObjective("Lives"));

		Logger.info("Scoreborad created");
	}

	/**
	 * Update scoreboard
	 * 
	 * @param p Player
	 */
	public static void updatePlayer(EntityPlayer p) {
		Score s = sb.func_96529_a(p.getCommandSenderName(), sb.getObjective("Lives"));
		s.setScorePoints(Lives.getLives(p.getGameProfile().getId()));
	}
}
