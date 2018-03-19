package io.github.skylerdev.McWiki;

import org.json.simple.JSONObject;

/**
 * A JSON object, specifically formatted for use in minecraft.
 * @author skylerdev
 * 
 */
@SuppressWarnings({ "unchecked", "serial" })
public class MCJson extends JSONObject {

    public MCJson() {
        super();
    }

    public MCJson(String text) {
        super();
        setText(text);
    }
    
    public MCJson(String text, String color) {
        super();
        setText(text);
        setColor(color);
    }
    
    public MCJson(String text, MCFont font) {
        super();
        setText(text);
        applyFont(font);
    }
   
    private void applyFont(MCFont font) {
        setColor(font.getColor());
        setBold(font.isBold());
        setItalic(font.isItalic());
        setUnderlined(font.isUnderlined());
        setStrikethrough(font.isStrikethrough());
        
        setText(font.getPrefix() + this.get("text") + font.getSuffix());
                
        setClick(font.getClickAction(), font.getClickValue());
        setHover(font.getHoverAction(), font.getHoverValue());
        
       
    }

    public void setText(String t) {
        this.put("text", t);
    }

    public void setColor(String c) {
        this.put("color", c);
    }

    public void setBold(boolean b) {
        this.put("bold", b);
    }

    public void setUnderlined(boolean b) {
        this.put("underlined", b);
    }

    public void setItalic(boolean b) {
        this.put("italic", b);
    }
    
    public void setStrikethrough(boolean b) {
        this.put("strikethrough", b);
    }

    public void setClick(String action, String value) {
        JSONObject click = new JSONObject();
        click.put("action", action);
        click.put("value", value);

        this.put("clickEvent", click);

    }

    public void setHover(String action, String value) {

        JSONObject hover = new JSONObject();
        hover.put("action", action);
        hover.put("value", value);

        this.put("hoverEvent", hover);

    }

}
