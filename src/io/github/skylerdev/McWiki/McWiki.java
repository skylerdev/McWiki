package io.github.skylerdev.McWiki;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class McWiki extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger("McWiki");

    FileConfiguration config = getConfig();

    String lang = "en";

    @Override
    public void onEnable() {

        this.saveDefaultConfig();

        String clang = config.getString("language");

        if (clang.length() != 2) {
            LOGGER.log(Level.WARNING, "The language file has been configured incorrectly, using english.");
        } else {
            lang = clang;
        }
        

        // commands
        this.getCommand("wiki").setExecutor(new CommandWiki());
        this.getCommand("mcwiki").setExecutor(new CommandMeta());

        LOGGER.log(Level.INFO, "McWiki 2.0 loaded successfully.");

    }

    @Override
    public void onDisable() {

    }

}
