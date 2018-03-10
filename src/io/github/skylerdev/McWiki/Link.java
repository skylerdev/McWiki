package io.github.skylerdev.McWiki;

import org.jsoup.nodes.Element;

@SuppressWarnings("serial")
public class Link extends MCJson {

    final String defaultColor = "aqua";
    
    final String defaultArticleText = "Click to show this article.";


    public Link(Element e) {
        super();

        
        this.setText("[" + e.text() + "]");
        
        this.setColor(defaultColor);
        
        //is it an internal link?
        if(e.attr("href").startsWith("/")){
            this.setClick("run_command", "/wiki " + e.text());
            this.setHover("show_text", defaultArticleText);
        }else {
            this.setClick("open_url", e.attr("href"));
            this.setHover("show_text", "External Link");
        }
        
        
    
    }
    
    public Link(String text, String url) {
        this.setText(text);
        this.setColor("light_purple");
        this.setClick("open_url", url);
        this.setHover("show_text", "Opens this page in your web browser.");
        
    }

}
