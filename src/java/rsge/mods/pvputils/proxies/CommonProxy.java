package rsge.mods.pvputils.proxies;

/**
 * Common/Server proxy
 * 
 * @author Rsge
 */
public class CommonProxy {
	/**
	 * If proxy is clientside
	 * 
	 * @return false
	 */
	public boolean isClient() {
		return false;
	}

	/**
	 * If proxy is serverside
	 * 
	 * @return true
	 */
	public boolean isServer() {
		return true;
	}
}
