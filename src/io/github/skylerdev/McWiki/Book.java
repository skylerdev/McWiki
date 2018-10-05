package io.github.skylerdev.McWiki;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Book objects are wiki pages converted to book format.
 * 
 * @author skyler
 * @version 2018
 */
@SuppressWarnings("unchecked")
public class Book {

    MCFont link;
    MCFont bold;
    MCFont italic;
    MCFont header2;
    MCFont header3;
    
    List<String> bookPages;
    
    public Book(ConfigHandler config, Document doc, String title, String redirect, String url) {
        // config fonts
        link = config.getFont("a");
        bold = config.getFont("b");
        italic = config.getFont("i");
        header2 = config.getFont("h2");
        header3 = config.getFont("h3");
        
        bookPages = buildPages(doc, title, redirect, url);
        
    }
    
    public List<String> getPages(){
        return bookPages;
    }
    

    private List<String> buildPages(Document doc, String title, String redirect, String url) {

        ArrayList<String> pages = new ArrayList<String>();
        
        Elements main = doc.select("p, h2, h3");

        
        pages.add(titlePage(title, redirect, url));
        pages.add("Will be replaced with table of contents later");

        JSONArray contentsPage = newPage();
        MCJson contentsHead = new MCJson("Contents\n\n", "dark_gray");
        contentsHead.setBold(true);
        contentsPage.add(contentsHead);

        JSONArray currentPage = newPage();
        int currentPageSize = 0;
        int maxChars = 230;

        MCJson space = new MCJson(" ");
        MCJson newline = new MCJson("\n");

        boolean findNextHead = false;

        // For each content element
        for (Element mainchild : main) {

            // breakpage if over
            if (currentPageSize > maxChars) {
                pages.add(currentPage.toString());
                currentPageSize = 0;
                currentPage = newPage();
            }

            // Handle big header
            if (mainchild.is("h2")) {
                if (isOmitted(mainchild)) {
                    // omit all section content if one of ommitted sections
                    findNextHead = true;
                    continue;
                }
                // we found the next header, stop omitting
                findNextHead = false;

                // Newpage *always* for big headers
                pages.add(currentPage.toString());
                currentPage = newPage();
                currentPageSize = 20;

                String htext = mainchild.text().replaceAll("\\[edit\\]", "");
                currentPage.add(new MCJson(htext, header2));
                currentPage.add(space);
                currentPage.add(backButton());
                currentPage.add(newline);

                // add to contents
                MCJson contentsLink = new MCJson(htext, link);
                contentsLink.setHover("show_text", "Jump to this section");
                contentsLink.setClick("change_page", "" + (pages.size() + 1));

                contentsPage.add(contentsLink);
                contentsPage.add(new MCJson("\n"));

            } else if (mainchild.is("h3")) {
                // Handle little header

                String h = mainchild.text().replaceAll("\\[edit\\]", "");

                currentPage.add(new MCJson(h, header3));
                currentPage.add(space);
                currentPageSize += h.length() + 2;

            } else if (mainchild.is("p") && !findNextHead) {
                // Go through paragraph content
                List<Node> pelems = mainchild.childNodes();
                for (Node n : pelems) {

                    // breakpage if over
                    if (currentPageSize > maxChars) {
                        pages.add(currentPage.toString());
                        currentPageSize = 0;
                        currentPage = newPage();
                    }

                    // Element handler
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        MCJson json = new MCJson("");
                        
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
                            json = a;
                        } else if (e.is("b")) {
                            json = new MCJson(e.text(), bold);
                        } else if (e.is("i")) {
                            json = new MCJson(e.text(), italic);
                        }
 
                        currentPage.add(json);
                        String text = (String) json.get("text");
                        currentPageSize += text.length();

                    } else if (n instanceof TextNode) {
                        TextNode t = (TextNode) n;
                        String text = t.text();
                        int length = text.length();

                        if (currentPageSize + length > maxChars - 10) {

                            // Rare case handler
                            int splitAt = text.lastIndexOf(" ", maxChars - currentPageSize);
                            if (splitAt < 0) {
                                pages.add(currentPage.toString());
                                currentPage = newPage();
                                currentPage.add(new MCJson(text));
                                currentPageSize = length;

                            } else {
                                String firstString = text.substring(0, splitAt);
                                String nextString = text.substring(splitAt + 1);
                                currentPage.add(new MCJson(firstString));

                                pages.add(currentPage.toString());
                                currentPageSize = nextString.length();
                                currentPage = newPage();

                                currentPage.add(new MCJson(nextString));
                            }
                        } else {
                            currentPage.add(new MCJson(text));
                            currentPageSize += text.length();
                        }
                    }
                }

                // paragraph end
                currentPage.add("\n");
                currentPageSize += 20;
            } else if (mainchild.is("ul") || (mainchild.is("ol"))) {
                for (Element li : mainchild.getElementsByTag("li")) {
                    for (Node n : li.childNodes()) {
                        if (n instanceof TextNode) {
                            TextNode t = (TextNode) n;

                            String text = t.text();
                            int length = text.length();

                            if (currentPageSize + length > maxChars - 10) {
                            }
                        }
                    }
                }
            }
        }

        // Replace contents placeholder, add end
        pages.set(1, contentsPage.toString());
        pages.add(endPage(title, url));

        return pages;

    }

    /**
     * Title page generator for book.
     * 
     * @param aTitle
     *            article title
     * @param aurl
     *            article url
     * @return the JSONArray string of the title page
     */
    public String titlePage(String aTitle, String redirect, String aurl) {
        JSONArray titlepage = newPage();
        
        
        
        if (redirect.isEmpty()) {
            titlepage.add(new MCJson("\n"));
        } else {
            titlepage.add(new MCJson("    redirect: ", "gray"));
            titlepage.add(new MCJson(redirect, "blue"));
        }
        
        MCJson title = new MCJson("\n " + aTitle + "\n", bold);
        titlepage.add(title);
        
        titlepage.add(new MCJson("\n Images, embeds, \n infoboxes, and \n table data omitted. \n\n\n", "gray"));
        titlepage.add(new MCJson(" Generated by §lMCWiki§r\n\n\n      ", "dark_gray"));

        MCJson full = new MCJson("Full Article", link);
        full.setClick("open_url", aurl);
        full.setHover("show_text", "Open this article in your browser.");
        titlepage.add(full);

        return titlepage.toString();
    }
    

    /**
     * Helper method for buildPages.
     * 
     * @returns a default jsonarray
     */
    private JSONArray newPage() {
        JSONArray a = new JSONArray();
        a.add("");
        return a;
    }

    /**
     * Back button premade object.
     * 
     * @return the back to contents button
     */
    private MCJson backButton() {
        MCJson backButton = new MCJson("«", link);
        backButton.setHover("show_text", "Back to contents");
        backButton.setClick("change_page", "2");
        return backButton;
    }
    
    private boolean isOmitted(Element mainchild) {
        String text = mainchild.text();
        String[] omitted = { "Achievements", "Advancements", "Video", "History", "Gallery", "Navigation", "Contents",
                "Issues", "References", "Data values" };
        for (int i = 0; i < omitted.length; i++) {
            if (text.contains(omitted[i])) {
                return true;
            }
        }
        return false;
    }
    

    /**
     * End page generator for book.
     * 
     * @param atitle
     *            article title
     * @param aurl
     *            article url
     * @return the JSONArray string of the ending page
     */
    public String endPage(String atitle, String aurl) {
        JSONArray endpage = newPage();

        MCJson title = new MCJson(" >> End of article. \n\n\n");

        title.setColor("dark_aqua");

        MCJson start = new MCJson(" << Back to beginning? ", link);
        start.setClick("change_page", "1");
        start.setHover("show_text", "Jump back to start page.");

        endpage.add(title);
        endpage.add(start);

        return endpage.toString();

    }
}
