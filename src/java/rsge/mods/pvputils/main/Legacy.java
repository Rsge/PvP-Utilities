package rsge.mods.pvputils.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import rsge.mods.pvputils.config.Config;


/**
 * This class is just for saving old code, it doesn't have any real function
 * 
 * @author Rsge
 */
public class Legacy {
	private static HashMap<String, Byte> playerlives = new HashMap<String, Byte>();
	private static HashMap<UUID, Long> playerTimes = new HashMap<UUID, Long>();
	private static HashMap<UUID, Boolean> playerOnline = new HashMap<UUID, Boolean>();
	private static HashMap<UUID, Boolean> playerInSpawn = new HashMap<UUID, Boolean>();
	private static int spawnX;
	private static int spawnY;
	private static int spawnZ;
	@SuppressWarnings("unused")
	private static LocalDate lastDate;

	public static void init() throws IOException {
		// Figure out, why this isn't working...
		// Lives
		DataInputStream datain = new DataInputStream(new FileInputStream(Reference.lifeData));
		try{
			int entries = datain.readInt();
			for (int i = 0; i < entries; i++){
				String uuid = datain.readUTF();
				byte lives = datain.readByte();
				playerlives.put(uuid, lives);
				if (Config.debugLogging)
					Logger.info(uuid + " has " + lives + " lives left");
			}
		}
		catch (EOFException ex){
			if (Config.debugLogging)
				Logger.warn("lives.dat-File empty! (This is not an Error!)");
		}
		datain.close();

	}

	public static void save() {
		// Lives
		try{
			DataOutputStream dataout = new DataOutputStream(new FileOutputStream(Reference.lifeData));
			dataout.writeInt(playerlives.size());
			for (Entry<String, Byte> entry : playerlives.entrySet()){
				String uuid = entry.getKey();
				byte lives = entry.getValue();
				dataout.writeUTF(uuid);
				dataout.writeByte(lives);
			}
			dataout.flush();
			dataout.close();

			if (Config.debugLogging){
				DataInputStream datain = new DataInputStream(new FileInputStream(Reference.timeData));
				int entries = datain.readInt();
				for (int i = 1; i < entries; i++){
					String uuid = datain.readUTF();
					byte lives = datain.readByte();
					Logger.info("File: " + uuid + " has " + lives + " lives left");
				}
				datain.close();
			}
		}
		catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Old second Method
	 */
	public static void second() {
		// For each player...
		for (Entry<UUID, Long> entry : playerTimes.entrySet()){
			UUID u = entry.getKey();
			long t = entry.getValue();
			boolean on = playerOnline.get(u);

			// ... if player is online
			if (on){
				// Old Version of: Get the player entity from the player's UUID
				@SuppressWarnings("unchecked")
				List<EntityPlayer> ps = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
				for (EntityPlayer p : ps){
					UUID u2 = p.getGameProfile().getId();
					if (u.equals(u2)){

						// Stop time if player is in spawn area
						if (Config.stopInSpawn){

							// Much longer old version with square radius:
							int playerX = p.getPlayerCoordinates().posX;
							int playerY = p.getPlayerCoordinates().posY;
							int playerZ = p.getPlayerCoordinates().posZ;
							if (Config.constantExcessiveLogging)
								Logger.info(u.toString() + ": " + playerX + ", " + playerY + ", " + playerZ);
							int comparedX = playerX - spawnX;
							boolean inRangeX = comparedX >= -Config.stopInSpawnRadius && comparedX <= Config.stopInSpawnRadius;
							int comparedY = playerY - spawnY;
							boolean inRangeY = comparedY >= -Config.stopInSpawnRadius && comparedY <= Config.stopInSpawnRadius;
							int comparedZ = playerZ - spawnZ;
							boolean inRangeZ = comparedZ >= -Config.stopInSpawnRadius && comparedZ <= Config.stopInSpawnRadius;
							if (Config.constantExcessiveLogging)
								Logger.info(u.toString() + " ComparedX = " + comparedX + " ComparedY = " + comparedY + " ComparedZ = " + comparedZ);
							if (!inRangeX || !inRangeY || !inRangeZ){

								if (comparedX > Config.stopInSpawnRadius || comparedY > Config.stopInSpawnRadius || comparedZ > Config.stopInSpawnRadius){
									try{
										if (!playerInSpawn.get(u)){
											playerInSpawn.put(u, true);
											p.addChatMessage(new ChatComponentText("You left the spawnzone, your time started again.")
													.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
										}

										passTime(u, p, t);

										if (Config.constantExcessiveLogging)
											Logger.info(u.toString() + "'s second passed");
									}
									catch (NullPointerException ex){
										playerInSpawn.put(u, false);
									}
								}
								else{
									try{
										if (playerInSpawn.get(u)){
											playerInSpawn.put(u, false);
											p.addChatMessage(new ChatComponentText("You entered the spawnzone, your time stopped.")
													.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
										}

										if (Config.constantExcessiveLogging)
											Logger.info(u.toString() + "'s second didn't pass");
									}
									catch (NullPointerException ex){}
								}
							}
							else
								passTime(u, p, t);
						}
					}
				}
			}
		}
		lastDate = LocalDate.now();
	}

	private static void passTime(UUID u, EntityPlayer p, long t) {}
}
