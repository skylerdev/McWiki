package io.github.skylerdev.McWiki;

import org.json.simple.JSONObject;

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
