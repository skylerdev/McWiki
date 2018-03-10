package io.github.skylerdev.McWiki;

import org.jsoup.nodes.Element;

@SuppressWarnings("serial")
public class Italic extends MCJson {

    final String defaultArticleText = "Click to show this article.";


    public Italic(Element e) {
        super();
        
        this.setText(e.text()); 
        this.setItalic(true);

    }

}
