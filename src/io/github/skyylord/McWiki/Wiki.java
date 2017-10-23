package io.github.skyylord.McWiki;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Wiki extends JavaPlugin{
	
	//config setup:
	FileConfiguration config = getConfig(); 
	
	//on enable:
	  @Override
	    public void onEnable() {
		  //build config 
		  
		  config.addDefault("onFailure", "Links you to the wiki.");
		  config.addDefault("usageMessage", "/wiki [article]");
		  config.addDefault("messageToSend", "§b{article}§b: §o{link}");
		  config.options().copyDefaults(true);
		  saveConfig();
		 
		  
	    }
	    //on disable:
	    @Override
	    public void onDisable() {
	    	//cleanup "

	    }
	    
	    @Override
	    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    	if (cmd.getName().equalsIgnoreCase("wiki")) { 
	    		if(args.length == 0){
	    			sender.sendMessage(config.getString("onFailure"));
	    			sender.sendMessage(config.getString("usageMessage"));
	    			return true; 
	    		}
	    		String article = "";
	    		//for loop that takes arg, adds underscore;, doesnt for last arg;
	    		for(int i = 0; i < args.length; i++){	
	    			if(i == args.length-1){
	    				article = article + args[i];
	    			}else{
	    			 article = article + args[i] + "_";
	    			}
	    		}
	    		sender.sendMessage(wikiMessage(article));
	    		return true;
	    		}
	    	return false;
	    	
	    }
	    
	    private String wikiMessage(String article){
	    	String currentMessage = config.getString("messageToSend");
	    	currentMessage.replaceAll("(\\{article\\})", article);
	    	currentMessage.replaceAll("(\\{link\\})", "minecraft.gamepedia.com/" + article);
	    	
	    	return(currentMessage);
	    			    	    
	    }
	    
	     

}


