package io.github.skylerdev.McWiki;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class McWiki extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("McWiki");


    @Override
    public void onEnable() {

        saveDefaultConfig();
        //readConfig();
        
        this.getCommand("wiki").setExecutor(new CommandWiki());
        LOGGER.log(Level.INFO, "McWiki 2.0 loaded successfully.");

    }
    
    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equals("mcwiki")) {
            if(args.length != 0) {
                if(args[0].equals("reload")) {
                    sender.sendMessage("§aReloaded McWiki config.");
                    reload();
                    
                    return true;
                }
            }
            sender.sendMessage(this.toString() + ". Made with love by EdgyKid <3");
            return true;
        }
        return true;
        
    }

   
    public void reload() {
        reloadConfig();
    }
    /*
    public void readConfig() {
        String clang = config.getString("language");
        if (clang.length() != 2) {
            LOGGER.log(Level.WARNING, "The language file has been configured incorrectly. Using English instead.");
        } else {
            lang = clang;
        }
        
        int ccutoff = config.getInt("cutoff");
        if(ccutoff > 1) {
            cutoff = ccutoff;
        }
        
        boolean cbook = config.getBoolean("bookMode");
        bookMode = cbook;
        
    }
*/
    @Override
    public void onDisable() {

    }

}
