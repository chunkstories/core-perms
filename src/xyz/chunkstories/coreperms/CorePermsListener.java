package xyz.chunkstories.coreperms;

import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.player.PlayerChatEvent;

import xyz.chunkstories.coreperms.CorePermsPermissionsManager.Group;

public class CorePermsListener implements Listener {

	final CorePermsPlugin corePermsPlugin;
	
	public CorePermsListener(CorePermsPlugin corePermsPlugin) {
		this.corePermsPlugin = corePermsPlugin;
	}
	
	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		
		Group g = corePermsPlugin.permissionsManager.getPlayerGroup(event.getPlayer());
		System.out.println(g.prefix);
		event.setFormattedMessage((g.prefix == null ? "" : g.prefix) + event.getPlayer().getDisplayName() + (g.suffix == null ? "" : g.suffix) + " > " + event.getMessage());
	}
}
