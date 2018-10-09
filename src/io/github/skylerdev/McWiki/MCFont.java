package io.github.skylerdev.McWiki;

/**
 * Holds MCJson properties, and can be applied to MCJson objects.
 * See https://minecraft.gamepedia.com/Commands#Raw_JSON_text for details.
 * 
 * @author skylerdev
 * 
 */
public class MCFont {

    private String color = " ";
    private boolean bold = false;
    private boolean strikethrough = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean obfuscated = false;

    private String clickAction = "";
    private String clickValue = "";
    private String hoverAction = "";
    private String hoverValue = "";

    private String prefix = "";
    private String suffix = "";

    /**
     * Constructs an object of type MCFont.
     */
    public MCFont() {

    }

    /**
     * Get the prefix to be displayed whenever an MCJson using this type appears.
     * @return prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix to be displayed whenever an MCJson using this type appears.
     * @param prefix
     */
    public void setPrefix(String prefix) {

        this.prefix = prefix;
    }

    /**
     * Get the suffix to be displayed after an MCJson using this font.
     * @return suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Set the suffix to be displayed after an MCJson using this font.
     * @param suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Get the color to be displayed for an MCJson using this font.
     * @return color
     */
    public String getColor() {
        return color;
    }

    /**
     * Set the color to be displayed after an MCJson using this font.
     * @param color (must be valid)
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns this font's bold property.
     * @return bold 
     */
    public boolean isBold() {
        return bold;
    }

   /**
    * Sets this font's bold property to be true or false.
    * @param bold
    */
    public void setBold(boolean bold) {
        this.bold = bold;
    }

    /**
     * Returns this font's strikethrough property.
     * @return strikethrough 
     */
    public boolean isStrikethrough() {
        return strikethrough;
    }
    
    /**
     * Sets this font's strikethrough property to be true or false.
     * @param strikethrough
     */
    public void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    /**
     * Returns this font's italic property.
     * @return italic 
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Sets this font's italic property to be true or false.
     * @param italic
     */
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    /**
     * Returns this font's underlined property.
     * @return underlined 
     */
    public boolean isUnderlined() {
        return underlined;
    }
    
    /**
     * Sets this font's underlined property to be true or false.
     * @param underlined
     */
    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    /**
     * Returns this font's bold property.
     * @return obfuscated 
     */
    public boolean isObfuscated() {
        return obfuscated;
    }

    /**
     * Sets this font's obfuscated property to be true or false.
     * @param obfuscated
     */
    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    /**
     * Gets this clickAction value.
     * @return clickAction
     */
    public String getClickAction() {
        return clickAction;
    }

    /**
     * Sets this font's clickaction.
     * Must be one of: open_url, open_file, run_command, change_page, suggest_command
     * @param valid clickaction 
     */
    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    /**
     * Gets this font's click value.
     * @return clickValue
     */
    public String getClickValue() {
        return clickValue;
    }

    /**
     * Sets this font's bold property to be true or false.
     * @param bold
     */
    public void setClickValue(String clickValue) {
        this.clickValue = clickValue;
    }

    /**
     * Get this font's hover action string.
     * @return hoverAction
     */
    public String getHoverAction() {
        return hoverAction;
    }

    /**
     * Sets this font's hoverAction.
     *  Valid values are "show_text" (shows raw JSON text), "show_item" (shows the tooltip of an item which can have NBT tags), and "show_entity" (shows an entity's name, possibly its type, and its UUID).
     * @param hoverAction
     */
    public void setHoverAction(String hoverAction) {
        this.hoverAction = hoverAction;
    }

    /**
     * Get this font's hoverValue.
     * @return hoverValue
     */
    public String getHoverValue() {
        return hoverValue;
    }

    /**
     * Sets this font's hoverValue property.
     * @param hoverValue 
     */
    public void setHoverValue(String hoverValue) {
        this.hoverValue = hoverValue;
    }
    
    /**
     * Sets this font's click action and value properties, both of which must be valid.
     * @param action the clickAction
     * @param value the clickValue
     */
    public void setClick(String action, String value) {
        setClickAction(action);
        setClickValue(value);
    }
    
    /**
     * Sets this font's hover action and value properties, both of which must be valid.
     * @param action the hoverAction
     * @param value the hoverValue
     */
    public void setHover(String action, String value) {
        setHoverAction(action);
        setHoverValue(value);
    }

}
