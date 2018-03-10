package io.github.skylerdev.McWiki;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class CommandWiki implements CommandExecutor {

    //private static final Logger LOGGER = Logger.getLogger("McWiki");
   //private static final Plugin mcwiki = Bukkit.getPluginManager().getPlugin("McWiki");

    final String selector = "div[id=mw-content-text] > p";
    

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            FileConfiguration config = Bukkit.getPluginManager().getPlugin("McWiki").getConfig();
            
            final String article = underscore(args);

            final String lang = config.getString("language");
        //    final boolean bookMode = McWiki.getBookMode();
            final int cutoff = config.getInt("cutoff");
            
            final String articleurl;
            if(lang.equals("en")) {
                articleurl = "http://www.minecraft.gamepedia.com/" + article;
            }else {
                articleurl = "http://www.minecraft-"+lang+".gamepedia.com/" + article;
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

                    sender.sendMessage("§d>>>  MCWIKI: " + title + "§d  <<<");

                    Elements main = doc.select(selector);
                    JSONArray json = new JSONArray();
                    json.add(""); // resets inheritance

                    int lines = 0;
                    for (Element p : main) {
                        List<Node> inner = p.childNodes();
                        for (Node n : inner) {
                            if (n instanceof Element) {

                                Element e = (Element) n;

                                if (e.is("a")) {
                                    Link l = new Link(e);
                                    json.add(l);

                                } else if (e.is("b")) {
                                    Bold b = new Bold(e);
                                    json.add(b);

                                } else if (e.is("i")) {
                                    Italic i = new Italic(e);
                                    json.add(i);

                                } else if (e.is("span")) {
                                    // ignore span for now

                                }

                            }
                            if (n instanceof TextNode) {
                                JSONObject text = new JSONObject();
                                text.put("text", ((TextNode) n).text());

                                json.add(text);

                            }
                        }

                        json.add("\n");
                        lines++;
                        // cutoff
                        if (lines > cutoff) {
                            Link pagelink = new Link("§d>>>   Limit reached. Click me to read more   <<<", articleurl);
                            json.add(pagelink);
                            break;
                        }
                    }

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "tellraw " + sender.getName() + " " + json.toString());

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
                    // Probably not the right way to do this xdddd
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
