package dev.hcr.penocide;

import dev.hcr.penocide.commands.HideStaffCommand;
import dev.hcr.penocide.commands.StaffModuleCommand;
import dev.hcr.penocide.commands.VanishCommand;
import dev.hcr.penocide.handler.StaffManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Penocide extends JavaPlugin {
    @Getter private static Penocide plugin;
    @Getter private StaffManager staffManager;

    @Override
    public void onEnable() {
        plugin = this;
        staffManager = new StaffManager(this);
        getCommand("hidestaff").setExecutor(new HideStaffCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("module").setExecutor(new StaffModuleCommand(this));
    }

    @Override
    public void onDisable() {
        staffManager.getStaffModule().forEach(player -> staffManager.setStaffModule(player, false, true));
        plugin = null;
    }
}
