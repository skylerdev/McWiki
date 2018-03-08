package io.github.skylerdev.McWiki;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CommandWiki implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger("McWiki");

    private static String selector = "div[id=mw-content-text] > p";
    
    

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wiki")) {
            if (args.length == 0) {
                return false;
            }

            final String article = underscore(args);

            String articleurl = "http://www.minecraft.gamepedia.com/" + article;

            sender.sendMessage("Getting " + article + "...");

            asyncFetchArticle(articleurl, new DocumentGetCallback() {

                @Override
                public void onQueryDone(Document doc) {
                    if (doc.baseUri().equals("404")) {
                        sender.sendMessage("404 error. Check the article name and try again.");
                        return;
                    }
                    LOGGER.log(Level.INFO, "Fetching " + doc.title());

                    Elements main = doc.select(selector);

                    for (Element e : main) {
                        sender.sendMessage(e.toString());

                    }
                }
            });

            return true;
        }
        return false;
    }

    private void asyncFetchArticle(final String url, final DocumentGetCallback callback) {
        // async run
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
            @Override
            public void run() {
                try {
                    final Document doc = Jsoup.connect(url).get();
                    // regular callback

                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("McWiki"), new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(doc);
                        }
                    });
                } catch (IOException e) {
                    // 404 or error, will get handled by callback
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
     * Replaces spaces with underscores.
     * 
     * @param String[]
     *            args
     * @return true if url was fetched successfully
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
