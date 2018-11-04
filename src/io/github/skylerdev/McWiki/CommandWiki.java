package io.github.skylerdev.McWiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.jsoup.select.Elements;

/**
 * CommandWiki is called whenever a user runs /wiki.
 * 
 * @author skyler
 * @version 2018
 */
public class CommandWiki implements CommandExecutor {

    private ConfigHandler config;

    private String lang;
    private boolean bookMode;
    private String domain;
    private String api;

    public CommandWiki(McWiki plugin) {
        config = plugin.getConfigHandler();
        getConfigValues();

    }
    
    public void reload() {
        getConfigValues();
    }

    /**
     * This method assigns current values to use from this plugins ConfigHandler.
     * It is called whenever the plugin is refreshed.
     */
    private void getConfigValues() {
        lang = config.getString("language");
        bookMode = config.getBool("bookmode");
        domain = config.getString("customsite");
        
        // If domain is default, then use custom language
        if (domain.equals("minecraft.gamepedia.com") && !lang.equals("en")) {
            domain = "https://minecraft-" + lang + ".gamepedia.com/";
        } else {
            domain = "https://" + domain + "/";
        }
        
        api = domain + "/api.php"; 
        
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            final String article = conjoin(args, "_");
            final String title = conjoin(args, " ");
            final String articleUrl = domain + article;      

            asyncFetchArticle(articleUrl, title, new DocumentGetCallback() {
                @Override
                public void onQueryDone(Document doc) {
                    if (doc == null) {
                        sender.sendMessage(
                                "§cERROR: Null pointer: Document returned was null. Java machine broke. This shouldnt happen. Check what your last command was and report to github.com/skylerdev");
                        return;
                    }

                    if (doc.baseUri().startsWith("ERROR")) {

                        switch (doc.baseUri()) {
                        case "ERROR999":
                            sender.sendMessage("§cERROR: IOException. Check console for details. ");
                            console.sendMessage("§c" + doc.text());
                            break;
                        case "ERROR555":
                            sender.sendMessage("§cERROR: ParserException: Recieved malformed JSON when trying to retrieve article name.");
                            sender.sendMessage("§c" + doc.text());
                            break;
                        case "ERROR404":
                            sender.sendMessage("§cArticle not found. Check the article name and try again.");
                            break;
                        case "ERROR000":
                            sender.sendMessage("§cERROR: Null pointer: Null pointer encountered while trying to fetch document.");
                            sender.sendMessage("§c" + doc.text());
                            break;
                        default:
                            sender.sendMessage("§cERROR: Generic error.");
                        }
                        return;
                    }

                    final String redirect = doc.getElementById("redirect").text();

                    // if mw-parser-output exists, use that instead (newer MediaWiki's use this)
                    Elements optionalOutput = doc.getElementsByClass("mw-parser-output");
                    if (!optionalOutput.isEmpty()) {
                        doc.selectFirst("body").html(optionalOutput.html());
                    }

                    doc.getElementsByTag("div").remove();
                    doc.getElementsByTag("table").remove();

                    if (bookMode) {
                        Book book = new Book(config, doc, redirect, articleUrl, domain);
                        List<String> pages = book.getPages();
                        showBook(pages, sender.getName());
                    } else {
                        Chat chat = new Chat(config, doc, redirect, articleUrl, domain);
                        JSONArray chatJson = chat.getJson();

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                "tellraw " + sender.getName() + " " + chatJson.toString());
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

                    // parse JSON string into object
                    String newTitle = title;
                    String redirectedFrom = " ";
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
                    doc.appendElement("div").attr("id", "redirect").text(redirectedFrom);
                    doc.title(newTitle);

                    
                } catch (final ParseException e) {
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            
                            callback.onQueryDone(new Document("ERROR555").appendText(text));
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
                } catch (final IOException e) {
                    // IOException
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            e.getMessage();
                            callback.onQueryDone(new Document("ERROR999"));
                        }
                    });
                } catch (final NullPointerException e) {
                    // Null pointer
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            e.getMessage();
                            callback.onQueryDone(new Document("ERROR000"));
                        }
                    });
                } finally {
                    
                    
                    
                    
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

}
