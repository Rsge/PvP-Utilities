package rsge.mods.pvputils.proxies;

public class ClientProxy extends CommonProxy {
	/**
	 * If proxy is clientside
	 * 
	 * @return true
	 */
	@Override
	public boolean isClient() {
		return true;
	}

	/**
	 * If proxy is serverside
	 * 
	 * @return false
	 */
	@Override
	public boolean isServer() {
		return false;
	}
}
