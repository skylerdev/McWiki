package io.github.skylerdev.McWiki;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * ConfigHandler is a helper class in conjuction with Bukkits built in FileConfiguration system.
 * 
 * @author skyler
 * @version 2018
 */
public class ConfigHandler {

    private static FileConfiguration config;
    private static McWiki plugin;
    private static Map<String, MCFont> fontMap = new HashMap<String, MCFont>();

    public ConfigHandler(McWiki mc) {
        plugin = mc;
        config = plugin.getConfig();
        
        loadFonts();

    }
    
    public FileConfiguration getConfig() {
        return config;
    }

    public void refreshConfig() {
        config = plugin.getConfig();
        loadFonts();
      
    }
    
    private void loadFonts() {
        constructFont("a");
        constructFont("b");
        constructFont("i");
        constructFont("h2");
        constructFont("h3");
    }
    
    private void constructFont(String fontTag) {
        fontMap.put(fontTag, getConfigFont(fontTag));
    }

    private MCFont getConfigFont(String e) {
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
    
    public String getLang() {
        return config.getString("language", "en");
    }
    
    public boolean getBook() {
        return config.getBoolean("bookmode", true);
    }
    
    public String getDomain() {
        return config.getString("customsite", "minecraft.gamepedia.com");
    }
    
    public MCFont getFont(String font) {
        return fontMap.get(font);
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
    
    /**
     * 
     * @param path
     * @return
     */
    public int getInt(String path){
        return config.getInt(path, 5);
    }



}
