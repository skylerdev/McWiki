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
import org.json.simple.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CommandWiki implements CommandExecutor {

    // private static final Logger LOGGER = Logger.getLogger("McWiki");
   // private static final Plugin mcwiki = Bukkit.getPluginManager().getPlugin("McWiki");

    final String selector = "div[id=mw-content-text] > p";

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            final FileConfiguration config = Bukkit.getPluginManager().getPlugin("McWiki").getConfig();

            final String article = underscore(args);

            final String lang = config.getString("language");
            final boolean bookMode = config.getBoolean("bookmode");
            final int cutoff = config.getInt("cutoff");

            final String articleurl;
            if (lang.equals("en")) {
                articleurl = "http://www.minecraft.gamepedia.com/" + article;
            } else {
                articleurl = "http://www.minecraft-" + lang + ".gamepedia.com/" + article;
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

                    // lets do the main (p) json first!
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
                                    Link l = new Link(e);
                                    line.add(l);
                                } else if (e.is("b")) {
                                    Bold b = new Bold(e);
                                    line.add(b);
                                } else if (e.is("i")) {
                                    Italic i = new Italic(e);
                                    line.add(i);
                                } else if (e.is("span")) {
                                    // ignore span for now.
                                }
                            }
                            if (n instanceof TextNode) {
                                JSONObject text = new JSONObject();
                                text.put("text", ((TextNode) n).text());
                                line.add(text);
                            }
                        }

                        line.add("\n");
                        json.add(line);

                    }

                    // Meta formatting for book or chat

  

                    if (bookMode) {

                        // this is raw because i hate consistency
                        JSONObject bookTop = new JSONObject();
                        bookTop.put("text", "§d §6§l" + title + "§d \n");
                        JSONObject bookBottom = new JSONObject();
                        bookBottom.put("text", "\n§d >> §6§lEnd of article§d << ");
                        
                        // pagesplitter

                        List<String> pages = new ArrayList<String>();
                        
                        
                        
                        // lets try 2 p's per page?
                        
                        for (int i = 0; i < json.size()-1; i += 2) {
                            TextComponent page = new TextComponent("");
                            if(i == 0) {
                                page.addExtra("");
                                page.addExtra(bookTop.toString());
                            }
                         
                            page.addExtra(json.get(i).toString());
                            page.addExtra(json.get(i + 1).toString());
                            pages.add(ComponentSerializer.toString(page));
                         
                        }
                        pages.add(bookBottom.toString());
                        
                        //build the book
                        
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        
                        //not using meta? 
                        
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        meta.setTitle("");
                        meta.setAuthor("");
                        BookUtil.setPages(meta, pages);
                        book.setItemMeta(meta);
                        
                        
                        BookUtil.openBook(book, Bukkit.getPlayer(sender.getName()));

                    } else {
                        
                        
                       

                        // chatcutoff handler
                        
                        MCJson chatBottom = new MCJson();
                        chatBottom.setClick("open_url", articleurl);
                        chatBottom.setColor("light_purple");
                        
                        if (cutoff < json.size()) {
                            chatBottom.setText(" >> Cutoff reached. Click for full article << ");  
                        }else {
                            chatBottom.setText(" >> End of article. << ");
                        }
                        
                        for (int i = json.size(); i < cutoff; i--) {
                            json.remove(i);

                        }

                        JSONObject chatTop = new JSONObject();
                        chatTop.put("text", "§d >> §6§l" + title + "§d << \n");

                        json.add(0, "");
                        json.add(1, chatTop);
                        json.add(json.size(), chatBottom);
                        
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + json.toString());
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
