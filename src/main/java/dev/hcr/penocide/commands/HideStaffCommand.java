package dev.hcr.penocide.commands;

import dev.hcr.penocide.Penocide;
import dev.hcr.penocide.utils.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideStaffCommand implements CommandExecutor {
    private final Penocide plugin;

    public HideStaffCommand(Penocide plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.getStaffManager().setStaffHidden(player, !plugin.getStaffManager().hasStaffHidden(player), true)) {
            player.sendMessage(StringUtils.format("&cCould not process staff command. Try again later."));
        }
        return false;
    }
}
