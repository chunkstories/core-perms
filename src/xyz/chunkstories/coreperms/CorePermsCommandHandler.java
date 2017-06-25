package xyz.chunkstories.coreperms;

import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.commands.Command;
import io.xol.chunkstories.api.plugin.commands.CommandEmitter;
import io.xol.chunkstories.api.plugin.commands.CommandHandler;
import io.xol.chunkstories.api.plugin.commands.ServerConsole;
import xyz.chunkstories.coreperms.CorePermsPermissionsManager.Group;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class CorePermsCommandHandler implements CommandHandler {

	CorePermsPlugin plugin;
	
	public CorePermsCommandHandler(CorePermsPlugin corePlugin)
	{
		this.plugin = corePlugin;
	}
	
	@Override
	public boolean handleCommand(CommandEmitter emitter, Command command, String[] arguments) {
		
		//Players are required a permission to enter commands but console bypasses those for obvious reasons
		if(!emitter.hasPermission("coreperms.admin") && !(emitter instanceof ServerConsole) )
		{
			emitter.sendMessage("You don't have the permission.");
			return true;
		}
		
		if(arguments.length == 0)
		{
			emitter.sendMessage("#00FFFFCorePerms admin interface");
			emitter.sendMessage("#00A0A0/perm reload");
			emitter.sendMessage("#00A0A0/perm info [player]");
			emitter.sendMessage("#00A0A0/perm stats");
		}
		else
		{
			String subCommand = arguments[0];
			
			if(subCommand.equals("reload"))
			{
				emitter.sendMessage("Reloading permissions...");
				long ms = System.currentTimeMillis();
				plugin.permissionsManager.loadPermissions();
				ms = System.currentTimeMillis() - ms;
				emitter.sendMessage("Done. ("+ms+" ms)");
			}
			else if(subCommand.equals("info"))
			{
				if(emitter instanceof Player)
				{
					Player player = (Player)emitter;
					Group g = plugin.permissionsManager.getPlayerGroup(player);
					emitter.sendMessage(player + " is in group " + g.name);
				}
			}
		}
		
		return true;
	}

}
