package io.github.skyylord.McWiki;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class Wiki extends JavaPlugin{
	//config setup:
	FileConfiguration config = getConfig(); 
	
	//on enable:
	  @Override
	    public void onEnable() {
		  //build
		  config.addDefault("failMessage", "Links you to the wiki.");
		  config.addDefault("usageMessage", "/wiki [article]");
		  config.addDefault("colorCode", "§b");
		  config.options().copyDefaults(true);
		  saveConfig();
		 
		  
	    }
	    //on disable:
	    @Override
	    public void onDisable() {
	    	//cleanup

	    }
	    
	    @Override
	    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    	if (cmd.getName().equalsIgnoreCase("wiki")) { 
	    		if(args.length != 1){
	    			sender.sendMessage(config.getString("failMessage"));
	    			sender.sendMessage(config.getString("usageMessage"));
	    			return false;
	    		}
	    		sender.sendMessage(config.getString("colorCode") + "minecraft.gamepedia.com/" + args[0]);
	    		return true;
	    	}
	    	return false;
	    }

}

