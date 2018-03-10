package io.github.skylerdev.McWiki;

import org.jsoup.nodes.Element;

@SuppressWarnings("serial")
public class Bold extends MCJson {
    

    final String defaultColor = "dark_aqua";
    
    final String defaultArticleText = "Click to show this article.";


    public Bold(Element e) {
        super();
        
        this.setText(e.text()); 
        this.setColor(defaultColor);
        this.setBold(true);

    }

}
