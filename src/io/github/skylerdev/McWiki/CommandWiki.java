package io.github.skylerdev.McWiki;

import java.io.IOException;
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
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@SuppressWarnings("unchecked")
public class CommandWiki implements CommandExecutor {

    private final String selector = "div[id=mw-content-text] > p";

    public ConfigHandler config;

    private String lang;
    private boolean bookMode;
    private int cutoff;
    private String curl;

    MCFont link;
    MCFont bold;
    MCFont italic;
    MCFont header1;
    MCFont header2;

    public CommandWiki(McWiki plugin) {
        config = new ConfigHandler(plugin);

        // config values
        lang = config.getString("language");
        bookMode = config.getBool("bookmode");
        cutoff = config.getCutoff();
        curl = config.getString("customsite");

        // config fonts
        link = config.constructFont("a");
        bold = config.constructFont("b");
        italic = config.constructFont("i");
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            final String article = underscore(args);
            final String articleurl;

            // if the domain is default but the language isnt
            if (curl.equals("minecraft.gamepedia.com") && !lang.equals("en")) {
                articleurl = "http://minecraft-" + lang + ".gamepedia.com/" + article;
            } else {
                articleurl = "http://" + curl + "/" + article;
            }

            asyncFetchArticle(articleurl, new DocumentGetCallback() {

                @Override
                public void onQueryDone(Document doc) {

                    String error = doc.baseUri();
                    if (error.startsWith("ERROR")) {
                        if (error.equals("ERROR404")) {
                            sender.sendMessage("§cERROR: 404. Check the article name and try again.");
                        } else if (doc.baseUri().equals("ERROR999")) {
                            sender.sendMessage("§cERROR: IOError: No connection. Talk to your admin.");
                        } else {
                            sender.sendMessage("§cERROR: HTTPError: Generic HTTP Error. Wait a while and try again.");
                        }
                        return;
                    }

                    String title = doc.getElementById("firstHeading").text();

                    JSONArray json = new JSONArray();
                    Elements main = doc.select(selector);

                    for (Element p : main) {
                        List<Node> inner = p.childNodes();
                        JSONArray line = new JSONArray();
                        line.add("");

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
                                        a.setClick("open_url", e.attr("href"));
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

                    if (bookMode) {

                        List<String> pages = new ArrayList<String>();
                        pages.add(titlePage(title, articleurl));
                        // for loop that adds one p (line) to page
                        for (int i = 0; i < json.size(); i++) {
                            pages.add(json.get(i).toString());
                        }
                        pages.add(endPage(title, articleurl));

                        showBook(pages, sender.getName());
                    } else {

                        MCJson chatBottom = new MCJson();
                        chatBottom.setClick("open_url", articleurl);
                        chatBottom.setHover("show_text", "Open this article in your browser.");
                        chatBottom.setColor("light_purple");

                        if (cutoff < json.size()) {
                            chatBottom.setText(" >> Cutoff reached. [Open in web] << ");
                        } else {
                            chatBottom.setText(" >> End of article. [Open in web] << ");
                        }

                        for (int i = json.size() - 1; i > cutoff; i--) {
                            json.remove(i);
                        }

                        MCJson chatTop = new MCJson("§d >> §6§l" + title + "§d << \n");

                        json.add(0, chatTop);
                        json.add(json.size(), chatBottom);

                        //...lol
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                "tellraw " + sender.getName() + " " + json.toString());
                    }
                }
            });

            return true;
        }
        return false;

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
    private void asyncFetchArticle(final String url, final DocumentGetCallback callback) {
        // async run
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
            @Override
            public void run() {
                try {
                    final Document doc = Jsoup.connect(url).get();
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
                    // No connection?
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(new Document("ERROR999"));
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
     *            The pages of the book to display.
     * @param playername
     *            The name of the player.
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
     * Helper method, replaces spaces with underscores.
     * 
     * @param String[]
     *            args
     */
    private String underscore(String[] args) {
        String a = "";
        for (int i = 0; i < args.length; i++) {
            if (i == args.length - 1) {
                a = a + args[i];
            } else {
                a = a + args[i] + "_";
            }
        }
        return a;
    }
    
    /**
     * Title page generator for book.
     * 
     * @param atitle
     * @param aurl
     * @return
     */
    public static String titlePage(String atitle, String aurl) {
        JSONArray titlepage = new JSONArray();
        titlepage.add("");

        MCJson title = new MCJson("\n   " + atitle, "dark_aqua");
        title.setBold(true);
        titlepage.add(title);

        titlepage.add("\n");

        MCJson subtitle = new MCJson("  Generated by McWiki", "dark_gray");
        titlepage.add(subtitle);

        titlepage.add("\n\n\n");

        MCJson sup = new MCJson("Displaying 1 p per page. Images and table data omitted.", "gray");
        titlepage.add(sup);
        titlepage.add("\n\n\n     ");

        MCJson link = new MCJson("[Full Article]");
        link.setClick("open_url", aurl);
        link.setHover("show_text", "Open this article in your browser.");
        link.setColor("aqua");
        titlepage.add(link);

        return titlepage.toString();
    }

    public static String endPage(String atitle, String aurl) {
        //TODO: Show article meta?
        
        MCJson title = new MCJson(" >> End of Article");

        title.setColor("dark_aqua");

        return title.toString();

    }

}
