package io.github.skylerdev.McWiki;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {

    private static FileConfiguration config;
    private static McWiki plugin;

    public ConfigHandler(McWiki mc) {
        plugin = mc;
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void refreshConfig() {
        config = plugin.getConfig();
    }

    public MCFont constructFont(String e) {
        MCFont font = new MCFont();

        String path = "format." + e + ".";

        font.setBold(getBool(path + "bold"));
        font.setItalic(getBool(path + "italic"));
        font.setStrikethrough(getBool(path + "strikethrough"));
        font.setUnderlined(getBool(path + "underlined"));
        font.setObfuscated(getBool(path + "obfuscated"));

        font.setPrefix(getString(path + "prefix"));
        font.setSuffix(getString(path + "suffix"));
        font.setColor(getString(path + "color"));
        
        font.setClickAction(getString(path + "clickaction"));
        font.setClickValue(getString(path + "clickvalue"));
        
        font.setHoverAction(getString(path + "hoveraction"));
        font.setHoverValue(getString(path + "hovervalue"));
        
        return font;
    }
    
    /**
     * Retrieves string value from config. If nonexistent, returns empty string.
     */
    public String getString(String path) {
        return config.getString(path, "");
    }
    
    /**
     * Retrieves boolean value from config. If nonexistent, returns false.
     */
    public boolean getBool(String path) {
        return config.getBoolean(path, false);
    }
    
    public int getInt(String path){
        return config.getInt(path, 5);
    }



}
