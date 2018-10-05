package io.github.skylerdev.McWiki;

/**
 * Holds MCJson properties, and can be applied to MCJson objects.
 * 
 * @author skylerdev
 * 
 */
public class MCFont {

    /**
     * The color to be displayed, in hex char format.
     */
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
     * 
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
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Get the color to be displayed for an MCJson using this font.
     */
    public String getColor() {
        return color;
    }

    /**
     * Set the color to be displayed after an MCJson using this font.
     * 
     */
    public void setColor(String color) {
        this.color = color;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public String getClickAction() {
        return clickAction;
    }

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    public String getClickValue() {
        return clickValue;
    }

    public void setClickValue(String clickValue) {
        this.clickValue = clickValue;
    }

    public String getHoverAction() {
        return hoverAction;
    }

    public void setHoverAction(String hoverAction) {
        this.hoverAction = hoverAction;
    }

    public String getHoverValue() {
        return hoverValue;
    }

    public void setHoverValue(String hoverValue) {
        this.hoverValue = hoverValue;
    }
    
    public void setClick(String action, String value) {
        setClickAction(action);
        setClickValue(value);
    }
    
    public void setHover(String action, String value) {
        setHoverAction(action);
        setHoverValue(value);
    }

}
