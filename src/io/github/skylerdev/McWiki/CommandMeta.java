package io.github.skylerdev.McWiki;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandMeta implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equals("mcwiki")) {
            sender.sendMessage("McWiki 2.0 by EdgyKid");
        }
        return true;
    }
}
