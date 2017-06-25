package xyz.chunkstories.coreperms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.server.PermissionsManager;

public class CorePermsPermissionsManager implements PermissionsManager {

	final CorePermsPlugin corePermsPlugin;
	
	Map<String, Group> users = new HashMap<String, Group>();
	Set<Group> groups = new HashSet<Group>();
	
	Group defaultGroup;
	
	public class Group {
		final String name;
		final PermissionNode node;
		final Set<Group> parents;
		
		String prefix;
		String suffix;
		
		Group(String name)
		{
			this.name = name;
			
			this.node = new PermissionNode();
			this.parents = new HashSet<Group>();
		}
		
		public boolean hasPermissionNode(String[] permissionNode) {
			
			//Ask the node
			if(node.hasPermissionNode(permissionNode))
				return true;
			
			//Ask the parents :D
			for(Group parent : parents)
				if(parent.hasPermissionNode(permissionNode))
					return true;
			
			return false;
		}
	}
	
	class PermissionNode {
		
		boolean wildcard = false;
		Map<String, PermissionNode> children = new HashMap<String, PermissionNode>();
		
		public boolean hasPermissionNode(String[] permissionNode) {
			if(wildcard == true)
				return true;
			
			if(permissionNode.length == 0)
				return true;
			
			PermissionNode child = children.get(permissionNode[0]);
			
			if(child == null)
				return false;
			
			String[] lesserPermissions = new String[permissionNode.length - 1];
			for(int i = 0;  i < lesserPermissions.length; i++)
			{
				lesserPermissions[i] = permissionNode[i + 1];
			}
			
			return child.hasPermissionNode(lesserPermissions);
		}
		
		public void addNode(String[] permissionNode) {
			
			if(permissionNode.length > 0)
			{
				if(permissionNode.length == 1)
				{
					if(permissionNode[0].equals("*"))
					{
						wildcard = true;
					}
					else
					{
						children.put(permissionNode[0], new PermissionNode());
					}
				}
				else
				{
					PermissionNode child = children.get(permissionNode[0]);
					
					//Subnode doesn't exist ? Let's make it
					if(child == null)
					{
						child = new PermissionNode();
						children.put(permissionNode[0], child);
					}
					
					String[] lesserPermissions = new String[permissionNode.length - 1];
					for(int i = 0;  i < lesserPermissions.length; i++)
					{
						lesserPermissions[i] = permissionNode[i + 1];
					}
					
					child.addNode(lesserPermissions);
				}
			}
		}
	}
	
	public void loadPermissions() {
		users.clear();
		groups.clear();
		
		defaultGroup = null;

		System.out.println("[Core-Perms]Loading group permissions");
		
		File groupsFile = new File("plugins/Core-Perms/groups.cp");
		groupsFile.getParentFile().mkdirs();
		
		if(groupsFile.exists())
		{
			try 
			{
				FileInputStream fis = new FileInputStream(groupsFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				String line;
				
				while((line = reader.readLine()) != null)
				{
					if(line.startsWith("group"))
					{
						String[] s = line.split(" ");
						if(s.length >= 2)
						{
							String groupName = s[1];
						
							Group group = new Group(groupName);
							
							//Default group
							if(s.length >= 3 && s[2].equals("default"))
								defaultGroup = group;
							
							//Loop for members
							while(true)
							{
								line = reader.readLine().replace("\t", "");
								if(line == null || line.startsWith("end"))
									break;
								
								if(line.startsWith("node"))
								{
									s = line.split(" ");
									String node = s[1];
									
									group.node.addNode(node.split("[.]"));
								}
								else if(line.startsWith("parent"))
								{
									s = line.split(" ");
									String parentName = s[1];
									
									Group parent = null;
									for(Group group2 : groups)
									{
										if(group2.name.equals(parentName))
										{
											parent = group2;
											break;
										}
									}
									
									if(parent != null)
										group.parents.add(parent);
									else
										System.out.println("[Core-Perms]Couldn't find parent group "+parent);
								}
								else if(line.startsWith("default"))
								{
									defaultGroup = group;
								}
								else if(line.startsWith("prefix"))
								{
									group.prefix = line.substring("prefix ".length());
								}
								else if(line.startsWith("suffix"))
								{
									group.suffix = line.substring("suffix ".length());
								}
							}
							
							groups.add(group);
						}
					}
				}
				
				reader.close();
			}
			catch(IOException e)
			{
				
			}
		}
		
		File usersFile = new File("plugins/Core-Perms/users.cp");
		usersFile.getParentFile().mkdirs();
		
		if(usersFile.exists())
		{
			try 
			{
				FileInputStream fis = new FileInputStream(usersFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				String line;
				
				while((line = reader.readLine()) != null)
				{
					String[] s = line.split(" ");
					if(s.length >= 2)
					{
						String username = s[0];
						String groupName = s[1];
						
						Group group = null;
						for(Group group2 : groups)
						{
							if(group2.name.equals(groupName))
							{
								group = group2;
								break;
							}
						}
						
						if(group != null)
							users.put(username, group);
						else
							System.out.println("Can't add user '"+username+"' to group '"+groupName+"', group not defined.");
							
					}
					
				}
				
				reader.close();
			}
			catch(IOException e)
			{
				
			}
		}
		
		if(defaultGroup == null) {
			
			if(groups.size() > 0)
				defaultGroup = groups.iterator().next();
			else
			{
				System.out.println("[Core-Perms]No groups found, defining default group 'default' with no permissions !");
				defaultGroup = new Group("default");
				groups.add(defaultGroup);
			}
		}
		
		System.out.println("[Core-Perms]Done loading, "+users.size()+" users in "+groups.size() + " groups.");
	}
	
	public CorePermsPermissionsManager(CorePermsPlugin corePermsPlugin) {
		this.corePermsPlugin = corePermsPlugin;
	}

	@Override
	public boolean hasPermission(Player player, String permissionNode) {
		
		Group group = getPlayerGroup(player);
		
		//System.out.println(permissionNode + " ? " + group.hasPermissionNode(permissionNode.split("[.]")));
		
		return group.hasPermissionNode(permissionNode.split("[.]"));
	}

	public Group getPlayerGroup(Player player) {
		Group group = users.get(player.getName());
		if(group == null)
			group = defaultGroup;
		
		return group;
	}

}
