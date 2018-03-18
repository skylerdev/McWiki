package io.github.skylerdev.McWiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.json.simple.JSONArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class CommandWiki implements CommandExecutor {

    private final String selector = "div[id=mw-content-text] > p";

    private final McWiki plugin;
    public FileConfiguration config;

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

        this.plugin = plugin;
        this.config = plugin.getConfig();

        lang = config.getString("language");
        bookMode = config.getBoolean("bookmode");
        cutoff = config.getInt("cutoff");
        curl = config.getString("customsite");

        link = new MCFont();
        link.setColor("aqua");
        link.setPrefix("[");
        link.setSuffix("]");

        bold = new MCFont();
        bold.setBold(true);
        bold.setColor("dark_aqua");

        italic = new MCFont();
        italic.setItalic(true);

        header1 = new MCFont();
        header2 = new MCFont();

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
                articleurl = "http://www.minecraft-" + lang + ".gamepedia.com/" + article;
            } else {
                articleurl = "http://www." + curl + "/" + article;
            }

            asyncFetchArticle(articleurl, new DocumentGetCallback() {

                @SuppressWarnings("unchecked")
                @Override
                public void onQueryDone(Document doc) {
                    if (doc.baseUri().equals("404")) {
                        sender.sendMessage("404 error. Check the article name and try again.");
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
                                } else if (e.is("span")) {

                                }
                            }
                            if (n instanceof TextNode) {
                                line.add(new MCJson(((TextNode) n).text()));
                            }
                        }

                        line.add("\n");
                        json.add(line);
                    }

                    // Meta formatting for book or chat

                    if (bookMode) {

                        List<String> pages = new ArrayList<String>();
                        pages.add(BookDefaults.titlePage(title, articleurl));
                        for (int i = 0; i < json.size() - 1; i++) {
                            pages.add(json.get(i).toString());
                        }
                        pages.add(BookDefaults.endPage());

                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        meta.setTitle("");
                        meta.setAuthor("");
                        BookUtil.setPages(meta, pages);
                        book.setItemMeta(meta);
                        BookUtil.openBook(book, Bukkit.getPlayer(sender.getName()));

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
                } catch (IOException e) {
                    // ERROR. Must be handled by callback code.
                    // Probably not the right way to do this but oh well
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(new Document("404"));
                        }
                    });
                }

            }
        });
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
}
