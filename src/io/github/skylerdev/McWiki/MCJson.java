package io.github.skylerdev.McWiki;

import org.json.simple.JSONObject;

/**
 * A JSON object, specifically formatted for use in minecraft.
 * See https://minecraft.gamepedia.com/Commands#Raw_JSON_text for details.
 * @author skylerdev
 * 
 */
@SuppressWarnings({ "unchecked", "serial" })
public class MCJson extends JSONObject {

    /**
     * Creates a new, empty MCJson Object.
     */
    public MCJson() {
        super();
        setText("");
    }

    /**
     * Creates a new MCJson object holding some text.
     * @param text to display
     */
    public MCJson(String text) {
        super();
        setText(text);
    }
    
    /**
     * Creates a new MCJson object holding some text in a certain color.
     * @param text to display
     * @param color for text
     */
    public MCJson(String text, String color) {
        super();
        setText(text);
        setColor(color);
    }
    
    /**
     * Creates a new MCJson object with some text in a certain font.
     * @param text to display
     * @param font applied to text
     */
    public MCJson(String text, MCFont font) {
        super();
        setText(text);
        applyFont(font);
    }
    
    /**
     * Returns the actual text value stored by this MCJson object.
     * 
     */
    public String getText() {
     return (String) get("text");
    }
   
    /**
     * Applies an MCFont's properties to this MCJson object.
     * @param font object to apply
     */
    private void applyFont(MCFont font) {
        
        setColor(font.getColor());
        setBold(font.isBold());
        setItalic(font.isItalic());
        setUnderlined(font.isUnderlined());
        setStrikethrough(font.isStrikethrough());
        
        setText(font.getPrefix() + this.get("text") + font.getSuffix() + "§r");
                
        setClick(font.getClickAction(), font.getClickValue());
        setHover(font.getHoverAction(), font.getHoverValue());
        
       
    }

    /**
     * Set the text of this MCJson.
     * @param t 
     */
    public void setText(String t) {
        this.put("text", t);
    }

    /**
     * Set color of this MCJSon.
     * @param c
     */
    public void setColor(String c) {
        this.put("color", c);
    }

    /**
     * Set whether this MCJson is bold.
     * @param b
     */
    public void setBold(boolean b) {
        this.put("bold", b);
    }

    /**
     * Set whether this MCJson is underlined.
     * @param b
     */
    public void setUnderlined(boolean b) {
        this.put("underlined", b);
    }

    /**
     * Set whether this MCJson is italic.
     * @param b
     */
    public void setItalic(boolean b) {
        this.put("italic", b);
    }
    
    /**
     * Set whether this MCJson is strikethrough.
     * @param b
     */
    public void setStrikethrough(boolean b) {
        this.put("strikethrough", b);
    }

    /**
     * Set the click action for this MCJson object. 
     * Must be valid! See minecraft wiki for details.
     * @param action to occur
     * @param value of action
     */
    public void setClick(String action, String value) {
        JSONObject click = new JSONObject();
        click.put("action", action);
        click.put("value", value);

        this.put("clickEvent", click);

    }

    /**
     * Set the hover action for this MCJson object.
     * Must be valid! See minecraft wiki for details.
     * @param action to occur
     * @param value of action
     */
    public void setHover(String action, String value) {
        JSONObject hover = new JSONObject();
        hover.put("action", action);
        hover.put("value", value);

        this.put("hoverEvent", hover);

    }
    
    /**
     * Set the hover text for this MCJson object.
     * @param text
     */
    public void setHoverText(String text) {
        JSONObject hover = new JSONObject();
        hover.put("action", "show_text");
        hover.put("value", text);
        
        this.put("hoverEvent", hover);
        
    }

}
