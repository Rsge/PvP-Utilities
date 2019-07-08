package rsge.mods.pvputils.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import rsge.mods.pvputils.config.Config;


/**
 * This whole class is just for saving old code, it doesn't have any real function
 * 
 * @author Rsge
 */
public class Legacy
{
	private static HashMap<String, Byte> playerlives = new HashMap<String, Byte>();

	public static void init() throws IOException
	{
		// TODO Figure out, why this isn't working...
		// Lives
		DataInputStream datain = new DataInputStream(new FileInputStream(Reference.lifeData));
		try
		{
			int entries = datain.readInt();
			for (int i = 0; i < entries; i++)
			{
				String uuid = datain.readUTF();
				byte lives = datain.readByte();
				playerlives.put(uuid, lives);
				if (Config.debugLogging)
					Logger.info(uuid + " has " + lives + " lives left");
			}
		}
		catch (EOFException ex)
		{
			if (Config.debugLogging)
				Logger.warn("lives.dat-File empty! (This is not an Error!)");
		}
		datain.close();

	}

	public static void save()
	{
		// Lives
		try
		{
			DataOutputStream dataout = new DataOutputStream(new FileOutputStream(Reference.lifeData));
			dataout.writeInt(playerlives.size());
			for (Entry<String, Byte> entry : playerlives.entrySet())
			{
				String uuid = entry.getKey();
				byte lives = entry.getValue();
				dataout.writeUTF(uuid);
				dataout.writeByte(lives);
			}
			dataout.flush();
			dataout.close();

			if (Config.debugLogging)
			{
				DataInputStream datain = new DataInputStream(new FileInputStream(Reference.timeData));
				int entries = datain.readInt();
				for (int i = 1; i < entries; i++)
				{
					String uuid = datain.readUTF();
					byte lives = datain.readByte();
					Logger.info("File: " + uuid + " has " + lives + " lives left");
				}
				datain.close();
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}

	}
}
