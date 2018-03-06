package io.github.skyylord.McWiki;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class Wiki extends JavaPlugin{
    
	private static final Logger LOGGER = Logger.getLogger("McWiki");
	
	//config setup:
	FileConfiguration config = getConfig(); 
	
	String lang = "en";
	
	
	//on enable:
	  @Override
	    public void onEnable() {
		  //build config 
		  
		  config.addDefault("languge", "en");
		  config.addDefault("languge", "en");
		  config.addDefault("languge", "en");
		  config.options().copyDefaults(true);
		  saveConfig();
		 
		  //load config 
		  String clang = config.getString("language");
		  
		  if(clang.length() != 2) {
			  LOGGER.log(Level.WARNING, "The language file has been configured incorrectly, using english.");
		  }else {
			  lang = clang;
		  }
		  
		  
	    }
	  
	  
	    //on disable:
	    @Override
	    public void onDisable() {
	    	   

	    }
	    
	    
	    //command
	    @Override
	    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    	if (cmd.getName().equalsIgnoreCase("wiki")) { 
	    		if(args.length == 0){
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
	   
	    		 String url = "www.minecraft.gamepedia.com/" + article;
	    		 
	    		 Document doc = new Document("notfound");
				try {
					doc = Jsoup.connect(url).get();
					LOGGER.log(Level.INFO, "Connected to " + url);
				} catch (IOException e1) {
					LOGGER.log(Level.INFO, "Failed connection to " + url);
					e1.printStackTrace();
					return true;
				}				
			
				LOGGER.log(Level.INFO, doc.title());
				//Checks if the article is valid by checking if bold text is present in one of the paragraphs
				
				 Elements paragraphs = doc.select("p");
			        for(Element p : paragraphs) {
			            if(p.selectFirst("b") != null) {
			           System.out.println(p.toString());
			           return true;
			            }
			        }
				
			
			
	    		
	    		
	    		
	    		return true;
	    		}
	    	return false;
	    	
	    }
	    
	     

}


