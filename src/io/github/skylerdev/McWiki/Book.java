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

    private MCFont link;
    private MCFont bold;
    private MCFont italic;
    private MCFont header2;
    private MCFont header3;

    private List<String> bookPages;

    private String domain;

    public Book(ConfigHandler config, Document doc, String redirect, String url, String domain) {
        // config fonts
        link = config.getFont("a");
        bold = config.getFont("b");
        italic = config.getFont("i");
        header2 = config.getFont("h2");
        header3 = config.getFont("h3");

        this.domain = domain;

        bookPages = buildPages(doc, redirect, url);

    }

    public List<String> getPages() {
        return bookPages;
    }

    private List<String> buildPages(Document doc, String redirect, String url) {

        ArrayList<String> pages = new ArrayList<String>();
        ArrayList<JSONArray> toc = new ArrayList<JSONArray>();

        Elements main = doc.select("body > p, h2, h3, li");

        pages.add(titlePage(doc.title(), redirect, url));

        JSONArray contentsPage = newPage();
        MCJson contentsHead = new MCJson("Contents\n\n", "dark_gray");
        contentsHead.setBold(true);
        contentsPage.add(contentsHead);

        // toc placeholder pages
        int tocEntries = 0;
        int tocMaxPage = 8;
        int tocNextMax = 0;
        for (Element mainchild : main) {
            if (mainchild.is("h2") && !isOmitted(mainchild)) {
                tocEntries++;
                if (tocEntries > tocNextMax) {
                    tocNextMax += 8;
                    pages.add("placeholder");
                }
            }
        }
        int currentContentEntries = 0;

        JSONArray currentPage = newPage();
        int currentPageSize = 0;
        int maxChars = 230;

        boolean findNextHead = false;

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

                currentContentEntries++;
                // Newpage *always* for big headers, except for the first one 
               if(!currentPage.equals(newPage())) {
                pages.add(currentPage.toString());
                currentPage = newPage();
                currentPageSize = 20;
               }

                String htext = mainchild.text();
                currentPage.add(new MCJson(htext, header2));
                currentPage.add(" ");
                currentPage.add(backButton());
                currentPage.add("\n");

                // add to contents
                MCJson contentsLink = new MCJson(htext, link);
                contentsLink.setHoverText("Jump to this section");
                contentsLink.setClick("change_page", "" + (pages.size() + 1));

                contentsPage.add(contentsLink);
                contentsPage.add(new MCJson("\n"));

                if (currentContentEntries % tocMaxPage == 0) {
                    toc.add(contentsPage);
                    contentsPage = newPage();
                }

            } else if (mainchild.is("h3")) {
                // Handle little header

                String h = mainchild.text().replaceAll("\\[edit\\]", "");

                currentPage.add(new MCJson(h, header3));
                currentPage.add(" ");
                currentPageSize += h.length() + 2;

            } else if (mainchild.is("p, li") && !findNextHead) {

                List<Node> innerelems = mainchild.childNodes();

                if (mainchild.is("li")) {
                    currentPage.add("- ");
                    currentPageSize += 2;
                }

                for (Node n : innerelems) {

                    // breakpage if over
                    if (currentPageSize > maxChars) {
                        pages.add(currentPage.toString());
                        currentPageSize = 0;
                        currentPage = newPage();
                    }

                    // Element handler
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        MCJson eJson = new MCJson("");

                        if (e.is("a")) {
                            String linkto = e.attr("href");
                            eJson = new MCJson(e.text(), link);
                            if (linkto.contains(domain)) {
                                eJson.setClick("run_command", "/wiki " + linkto.substring(linkto.lastIndexOf("/") + 1));
                                eJson.setHoverText("Click to show this article.");
                                if (linkto.contains("redlink")) {
                                    eJson.setColor("dark_red");
                                    eJson.setHoverText("§cThis article does not exist.");
                                }
                            } else {
                                eJson.setClick("open_url", linkto);
                                eJson.setHoverText("External Link");
                            }
                           
                        } else if (e.is("b")) {
                            eJson = new MCJson(e.text(), bold);
                        } else if (e.is("i")) {
                            eJson = new MCJson(e.text(), italic);
                        } else if (e.is("span")) {
                            eJson = new MCJson(e.text());
                        }

                        if(eJson.getText().isEmpty()) {
                            continue;
                        }
                        currentPage.add(eJson);
                        String text = (String) eJson.get("text");
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
            }
        }

        toc.add(contentsPage);

        pages.add(currentPage.toString());
        // Replace contents placeholders, add end
        if (tocNextMax != 0) {
            if (tocEntries % tocMaxPage == 0) {
                for (int i = 1; i < ((tocEntries / tocMaxPage)) + 1; i++) {
                    pages.set(i, toc.get(i - 1).toString());
                }
            } else {
                for (int i = 1; i < ((tocEntries / tocMaxPage)) + 2; i++) {
                    pages.set(i, toc.get(i - 1).toString());
                }
            }
        }
        pages.add(endPage());

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

        MCJson title = new MCJson("\n " + aTitle + "\n", bold);
        titlepage.add(title);

        if (redirect.isEmpty()) {
            titlepage.add(new MCJson("\n\n"));
        } else {
            titlepage.add(new MCJson("\nRedirected from:\n", "gray"));
            titlepage.add(new MCJson(redirect, "blue"));
        }
        titlepage.add(new MCJson("\n Generated by §lMCWiki§r\n\n\n      ", "dark_gray"));

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

    /**
     * Checks if a header is omitted.
     * 
     * @param mainchild
     * @return
     */
    private boolean isOmitted(Element mainchild) {
        String text = mainchild.text();
        String[] omitted = { "Achievements", "Advancements", "Video", "History", "Gallery", "Navigation", "Contents",
                "Issues", "References", "Data values", "Trivia" };
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
     * @return the JSONArray string of the ending page
     */
    public String endPage() {
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
