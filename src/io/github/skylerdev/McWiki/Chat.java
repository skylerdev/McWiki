package io.github.skylerdev.McWiki;

import java.util.List;

import org.json.simple.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Chat objects are wiki pages converted to chat format.
 * 
 * @author skyler
 * @version 2018
 */
@SuppressWarnings("unchecked")
public class Chat {
    
    private final int cutoff;
    
    MCFont link;
    MCFont bold;
    MCFont italic;
    MCFont header2;
    MCFont header3;
    
    JSONArray chatJson;
    
    public Chat(ConfigHandler config, Document doc, String title, String redirect, String articleUrl) {
        
        link = config.getFont("a");
        bold = config.getFont("b");
        italic = config.getFont("i");
        header2 = config.getFont("h2");
        header3 = config.getFont("h3");
        
        cutoff = config.getInt("cutoff");
        
        chatJson = buildJson(doc);

        
        MCJson chatBottom = footer(articleUrl);
        if (cutoff < chatJson.size()) {
            chatBottom.setText(" >> Cutoff reached. [Open in web] << ");
        } else {
            chatBottom.setText(" >> End of article. [Open in web] << ");
        }

        // Chop chop
        for (int i = chatJson.size() - 1; i > cutoff; i--) {
            chatJson.remove(i);
        }

        MCJson chatTop = new MCJson("§d >> §6§l" + title + "§d << \n");
        chatJson.add(0, chatTop);
        chatJson.add(chatBottom);
        
    }
    
    public JSONArray getJson() {
        return chatJson;
    }
    
    private JSONArray buildJson(Document doc) {
        JSONArray json = new JSONArray();
        Elements main = doc.select("p");

        for (Element mainchild : main) {

            JSONArray line = new JSONArray();
            line.add("");

            if (mainchild.is("p")) {
                List<Node> inner = mainchild.childNodes();
                for (Node n : inner) {
                    if (n instanceof Element) {
                        Element e = (Element) n;

                        if (e.is("a")) {
                            String linkto = e.attr("href");
                            MCJson a = new MCJson(e.text(), link);
                            if (linkto.startsWith("/")) {
                                a.setClick("run_command", "/wiki " + linkto.substring(1));
                                a.setHover("show_text", "Click to show this article.");
                            } else {
                                a.setClick("open_url", linkto);
                                a.setHover("show_text", "External Link");
                            }
                            line.add(a);
                        } else if (e.is("b")) {
                            line.add(new MCJson(e.text(), bold));
                        } else if (e.is("i")) {
                            line.add(new MCJson(e.text(), italic));
                        }
                    }
                    if (n instanceof TextNode) {
                        line.add(new MCJson(((TextNode) n).text()));
                    }
                }
                line.add("\n");
                json.add(line);
            }

        }

        return json;
    }
    
    public MCJson footer(String url) {
        MCJson chatBottom = new MCJson();
        chatBottom.setClick("open_url", url);
        chatBottom.setHover("show_text", "Open this article in your browser.");
        chatBottom.setColor("light_purple");
        return chatBottom;
    }
    
    
}
