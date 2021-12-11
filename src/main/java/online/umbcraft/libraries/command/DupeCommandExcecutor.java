package online.umbcraft.libraries.command;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DupeCommandExcecutor implements CommandExecutor  {

    final private GoldenDupes plugin;

    public DupeCommandExcecutor(GoldenDupes plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        switch(args.length) {
            case 0:
                return pluginInfo(sender);
            case 1:
                switch(args[0]) {
                    case "reload":
                        return reloadPlugin(sender);
                    case "commands":
                        return listCommands(sender);
                    case "info":
                        return pluginInfo(sender);
                    default:
                        return invalidCommand(sender);
                }
            default:
                return invalidCommand(sender);
        }
    }

    public boolean listCommands(CommandSender sender) {
        sender.sendMessage(
                String.format("%sCommands:", ChatColor.GOLD)
        );
        sender.sendMessage(
                String.format("%s/gd info - gives basic info about the plugin", ChatColor.GOLD)
        );
        sender.sendMessage(
                String.format("%s/gd reload - reloads the goldendupes config", ChatColor.GOLD)
        );
        sender.sendMessage(
                String.format("%s/gd commands - lists all gd commands", ChatColor.GOLD)
        );
        return true;
    }

    public boolean invalidCommand(CommandSender sender) {
        sender.sendMessage(
                String.format("%sInvalid command. view a list of all commands with /gd commands", ChatColor.GOLD)
        );
        return false;
    }

    public boolean reloadPlugin(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(
                String.format("%sGolden Dupes has been reloaded!", ChatColor.GOLD)
        );
        return true;
    }

    public boolean pluginInfo(CommandSender sender) {
        sender.sendMessage(
                String.format("%sGolden Dupes Plugin %sv%s", ChatColor.GOLD, ChatColor.RED, plugin.getDescription().getVersion())
        );
        sender.sendMessage(
                String.format("%sA lightweight dupe recreation plugin.", ChatColor.GOLD)
        );
        sender.sendMessage(
                String.format("%sAuthor(s): %s", ChatColor.GOLD, plugin.getDescription().getAuthors())
        );
        sender.sendMessage(
                String.format("%sGet support here: %s%s", ChatColor.GOLD, ChatColor.GREEN, "https://discord.com/invite/Fe7FXuEsPs")
        );
        sender.sendMessage(
                String.format("%sReport an issue here: %s%s", ChatColor.GOLD, ChatColor.GREEN, "https://github.com/Ivan8or/GoldenDupes/issues/new")
        );
        sender.sendMessage(
                String.format("%srun %s/gd commands %sfor a list of commands.", ChatColor.GOLD, ChatColor.RED, ChatColor.GOLD)
        );
        return true;
    }
}
