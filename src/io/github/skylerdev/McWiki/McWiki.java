package io.github.skylerdev.McWiki;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class McWiki extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("McWiki");

    @Override
    public void onEnable() {
        
        saveDefaultConfig();
        
        this.getCommand("wiki").setExecutor(new CommandWiki(this));
        
        LOGGER.log(Level.INFO, "[MCWiki] Loaded " + toString() + " successfully.");

    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("mcwiki")) {
            if (args.length == 0) {
                displayHelp(sender);
            } else {
                if (args[0].equals("reload")) {
                    sender.sendMessage("§aReloaded McWiki config.");
                    reload();
                    return true;
                }
                if (args[0].equals("help")) {
                    displayHelp(sender);
                }
            }
            
            return true;
        }
        
        return true;
    }

    public void displayHelp(CommandSender sender) {
        sender.sendMessage("§d" + this.toString());
        sender.sendMessage("§7By skylerdev");
        sender.sendMessage("§f/wiki <article>");
        sender.sendMessage("§f/mcwiki <help/reload>");
    }

    public void reload() {
        reloadConfig();
        this.getCommand("wiki").setExecutor(new CommandWiki(this));
    }

    @Override
    public void onDisable() {

    }
}
