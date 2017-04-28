package xyz.chunkstories.coreperms;

import io.xol.chunkstories.api.plugin.PluginInformation;
import io.xol.chunkstories.api.plugin.ServerPlugin;
import io.xol.chunkstories.api.server.ServerInterface;

public class CorePermsPlugin extends ServerPlugin {

	final CorePermsPermissionsManager permissionsManager;
	
	public CorePermsPlugin(PluginInformation pluginInformation, ServerInterface clientInterface) {
		super(pluginInformation, clientInterface);
		
		permissionsManager = new CorePermsPermissionsManager(this);
	}

	@Override
	public void onEnable() {
		System.out.println("Enabling Core-Perms " + this.getPluginInformation().getPluginVersion());
		
		permissionsManager.loadPermissions();
		this.getServer().installPermissionsManager(permissionsManager);
		this.getPluginManager().registerCommandHandler("perm", new CorePermsCommandHandler(this));
		this.getPluginManager().registerEventListener(new CorePermsListener(this), this);
	}

	@Override
	public void onDisable() {
		System.out.println("Disabling Core-Perms");
		
	}

}
