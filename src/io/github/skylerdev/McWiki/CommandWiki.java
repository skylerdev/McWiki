package io.github.skylerdev.McWiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@SuppressWarnings("unchecked")
public class CommandWiki implements CommandExecutor {

    public ConfigHandler config;

    private String lang;
    private boolean bookMode;
    private int cutoff;
    private String domain;

    MCFont link;
    MCFont bold;
    MCFont italic;
    MCFont header2;
    MCFont header3;
    MCFont basetext;

    final String api = "https://minecraft.gamepedia.com/api.php";

    public CommandWiki(McWiki plugin) {
        config = new ConfigHandler(plugin);

        // config values
        lang = config.getString("language");
        bookMode = config.getBool("bookmode");
        cutoff = config.getInt("cutoff");
        domain = config.getString("customsite");

        // config fonts
        link = config.constructFont("a");
        bold = config.constructFont("b");
        italic = config.constructFont("i");
        header2 = config.constructFont("h2");
        header3 = config.constructFont("h3");

    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            final String article = conjoin(args, "_");
            final String title = conjoin(args, " ");
            final String articleUrl;

            // If domain is default, then use custom language
            if (domain.equals("minecraft.gamepedia.com") && !lang.equals("en")) {
                articleUrl = "https://minecraft-" + lang + ".gamepedia.com/" + article;
            } else {
                articleUrl = "http://" + domain + "/" + article;
            }

            asyncFetchArticle(articleUrl, title, new DocumentGetCallback() {
                @Override
                public void onQueryDone(Document doc) {
                    if (doc == null) {
                        sender.sendMessage(
                                "§cERROR: Null pointer: Document returned was null. Check what your last command was and report to github.com/skylerdev");
                        return;
                    }

                    switch (doc.baseUri()) {
                    case "ERROR999":
                        sender.sendMessage(
                                "§cERROR: IOException: Might wanna narrow this down.");
                        break;
                    case "ERROR555":
                        sender.sendMessage(
                                "§cERROR: ParseJSONException: Recieved malformed JSON when trying to retrieve article name.");
                        break;
                    case "ERROR404":
                        sender.sendMessage(
                                "§cArticle not found. Check the article name and try again.");
                        break;
                    case "ERROR000":
                        sender.sendMessage(
                                "§cERROR: Null pointer: Null pointer encountered while trying to fetch document.");
                        break;
                    default:
                        if(doc.baseUri().startsWith("ERROR")) {
                        sender.sendMessage("§cERROR: Generic error.");
                        }
                        break;
                    }

                    String aTitle = doc.title();
                    Elements mainp = doc.select("p");
                    
                    //if mw-parser-output exists, use that instead (newer MediaWikis use this)
                    Elements optionalOutput = doc.getElementsByClass("mw-parser-output");
                    if(!optionalOutput.isEmpty()) {
                        doc.html(optionalOutput.get(0).html());
                    }

                    if (bookMode) {
                        List<String> pages = buildPages(doc, aTitle, articleUrl);
                        showBook(pages, sender.getName());
                    } else {
                        JSONArray chatJson = chatJson(mainp);
                        MCJson chatBottom = chatBottom(articleUrl);

                        if (cutoff < chatJson.size()) {
                            chatBottom.setText(" >> Cutoff reached. [Open in web] << ");
                        } else {
                            chatBottom.setText(" >> End of article. [Open in web] << ");
                        }

                        // Chop chop
                        for (int i = chatJson.size() - 1; i > cutoff; i--) {
                            chatJson.remove(i);
                        }

                        MCJson chatTop = new MCJson("§d >> §6§l" + aTitle + "§d << \n");
                        chatJson.add(0, chatTop);
                        chatJson.add(chatBottom);

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                "tellraw " + sender.getName() + " " + chatJson.toString());
                    }
                }
            });

            return true;
        }
        return false;

    }

    private List<String> buildPages(Document doc, String title, String url) {

        Elements main = doc.select("p, h2, h3");

        ArrayList<String> pages = new ArrayList<String>();
        pages.add(titlePage(title, url));
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
                    //omit all section content if one of ommitted sections
                    findNextHead = true;
                    continue;
                }
                //we found the next header, stop omitting
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
                            currentPage.add(a);

                        } else if (e.is("b")) {
                            currentPage.add(new MCJson(e.text(), bold));

                        } else if (e.is("i")) {
                            currentPage.add(new MCJson(e.text(), italic));
                        }

                        currentPageSize += e.text().length();

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
            }
        }

        // Replace contents placeholder, add end
        pages.set(1, contentsPage.toString());
        pages.add(endPage(title, url));

        return pages;

    }

    private boolean isOmitted(Element mainchild) {
        String text = mainchild.text();
        String[] omitted = { "Achievements", "Advancements", "Video", "History", "Gallery", "Navigation", "Contents",
                "Issues", "References" };
        for (int i = 0; i < omitted.length; i++) {
            if (text.contains(omitted[i])) {
                return true;
            }
        }
        return false;
    }

    private MCJson chatBottom(String url) {
        MCJson chatBottom = new MCJson();
        chatBottom.setClick("open_url", url);
        chatBottom.setHover("show_text", "Open this article in your browser.");
        chatBottom.setColor("light_purple");
        return chatBottom;
    }

    private JSONArray chatJson(Elements main) {
        JSONArray json = new JSONArray();

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
     * Fetches the article from the web, asynchronously.
     * 
     * @param url
     *            url of article to fetch.
     * @param callback
     *            callback to implement when done fetching.
     * 
     */
    private void asyncFetchArticle(final String url, final String title, final DocumentGetCallback callback) {
        // async run
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
            @Override
            public void run() {
                try {
                    // query API, get JSON string
                    StringBuilder result = new StringBuilder();
                    URL apiurl = new URL(api + "?action=query&titles=" + title + "&redirects=true&format=json");
                    HttpURLConnection conn = (HttpURLConnection) apiurl.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();
                    
                    Logger.getLogger("McWiki").log(Level.INFO, url + " " + title + " " + result.toString());

                    // parse JSON string into object
                    String newTitle = "";
                    String redirectedFrom = "";
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(result.toString());

                    // parse JSON object into title and redirect, if exists
                    JSONObject query = (JSONObject) json.get("query");
                    JSONArray normalized = (JSONArray) query.get("normalized");
                    JSONArray redirects = (JSONArray) query.get("redirects");
                    if (redirects != null) {
                        JSONObject redirectsActual = (JSONObject) redirects.get(0);
                        redirectedFrom = (String) redirectsActual.get("from");
                        newTitle = (String) redirectsActual.get("to");
                    } else if (normalized != null) {
                        JSONObject normalizedActual = (JSONObject) normalized.get(0);
                        newTitle = (String) normalizedActual.get("to");
                    }

                    // get raw html, redirect + title metadata
                    final Document doc = Jsoup.connect(url).data("action", "render").get();
                    doc.append("<div id='redirect'>" + redirectedFrom + "</div>");
                    doc.title(newTitle);

                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(doc);

                        }
                    });
                } catch (final HttpStatusException e) {
                    // Http Status error.
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(new Document("ERROR" + e.getStatusCode()));
                        }
                    });
                } catch (IOException e) {
                    // IOException
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(new Document("ERROR999"));
                        }
                    });
                } catch (ParseException e) {
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(new Document("ERROR555"));
                        }
                    });
                }

            }
        });
    }

    /**
     * Shows book using BookUtil reflection class.
     * 
     * @param pages
     *            The pages of the book to display in List<String>
     * @param playername
     *            The name of the player
     */
    private void showBook(List<String> pages, String playername) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("");
        meta.setAuthor("");
        BookUtil.setPages(meta, pages);
        book.setItemMeta(meta);
        BookUtil.openBook(book, Bukkit.getPlayer(playername));
    }

    /**
     * Helper method, joins args to make String.
     * 
     * @param String[]
     *            Array of strings to conjoin with a value
     */
    private String conjoin(String[] args, String value) { 
        String a = args[0];
        for (int i = 1; i < args.length; i++) {
            a = a + value + args[i];
        } 
        return a;
    }

    /**
     * Title page generator for book.
     * 
     * @param atitle
     *            article title
     * @param aurl
     *            article url
     * @return the JSONArray string of the title page
     */
    public String titlePage(String atitle, String aurl) {
        JSONArray titlepage = newPage();

        MCJson title = new MCJson("\n " + atitle + "\n\n", bold);
        titlepage.add(title);

        titlepage.add(new MCJson(" Images, embeds, \n infoboxes, and \n table data omitted. \n\n\n", "gray"));
        titlepage.add(new MCJson(" Generated by §lMCWiki§r\n\n\n      ", "dark_gray"));

        MCJson full = new MCJson("Full Article", link);
        full.setClick("open_url", aurl);
        full.setHover("show_text", "Open this article in your browser.");
        titlepage.add(full);

        return titlepage.toString();
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
