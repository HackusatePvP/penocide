package dev.hcr.penocide.handler;

import dev.hcr.penocide.Penocide;
import dev.hcr.penocide.events.HideStaffEvent;
import dev.hcr.penocide.events.StaffModuleEnterEvent;
import dev.hcr.penocide.events.StaffModuleLeaveEvent;
import dev.hcr.penocide.events.StaffVanishEvent;
import dev.hcr.penocide.utils.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
public class StaffManager {
    private final Collection<Player> staffModule = new HashSet<>();
    private final Collection<Player> vanish = new HashSet<>();
    private final Collection<Player> hideStaff = new HashSet<>();
    private final Map<Player, ItemStack[]> inventory = new HashMap<>();
    private final Map<Player, ItemStack[]> armor = new HashMap<>();
    private final Map<Player, Long> cooldown = new HashMap<>(); //FIXME: I highly recommend removing this as this is just to show how cancelling an event works

    public StaffManager(Penocide plugin) {
        Bukkit.getPluginManager().registerEvents(new StaffListener(plugin), plugin);
    }

    public boolean setStaffModule(Player player, boolean m, boolean bypass) {
        if (bypass) {
            if (m) {
                staffModule.add(player);
                setVanish(player, true, true);
                inventory.put(player, player.getInventory().getContents());
                armor.put(player, player.getInventory().getArmorContents());
                player.getInventory().clear();
                applyStaffItems(player);
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage(StringUtils.format("&aYou have entered the staff module."));
            } else {
                staffModule.remove(player);
                setVanish(player, false, true);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().setContents(inventory.get(player));
                player.getInventory().setArmorContents(armor.get(player));
                player.sendMessage(StringUtils.format("&cYou have left the staff module."));
            }
            return true;
        }
        if (m) {
            StaffModuleEnterEvent event = new StaffModuleEnterEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            staffModule.add(player);
            setVanish(player, true, true);
            inventory.put(player, player.getInventory().getContents());
            armor.put(player, player.getInventory().getArmorContents());
            player.getInventory().clear();
            applyStaffItems(player);
            player.setGameMode(GameMode.CREATIVE);
        } else {
            StaffModuleLeaveEvent event = new StaffModuleLeaveEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            staffModule.remove(player);
            setVanish(player, false, true);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().setContents(inventory.get(player));
            player.getInventory().setArmorContents(armor.get(player));
        }
        return true;
    }

    public boolean setVanish(Player player, boolean v, boolean bypass) {
        if (bypass) {
            if (v) {
                vanish.add(player);
                Bukkit.getOnlinePlayers().forEach(player1 -> player1.hidePlayer(player));
                Bukkit.getOnlinePlayers().stream().filter(player1 -> player1.hasPermission("penoicide.staff")).filter(player1 -> !hideStaff.contains(player1)).forEach(player1 -> player1.showPlayer(player));
                player.sendMessage(StringUtils.format("&aYou are now hidden."));
            } else {
                vanish.remove(player);
                Bukkit.getOnlinePlayers().forEach(player1 -> player1.showPlayer(player));
                player.sendMessage(StringUtils.format("&cYou are now visible."));
            }
            return true;
        }
        if (v) {
            StaffVanishEvent event = new StaffVanishEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            vanish.add(player);
        } else {
            // FIXME: I don't see a point in cancelling a Vanish command when a player is unvanishing.
            StaffVanishEvent event = new StaffVanishEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            vanish.remove(player);
            Bukkit.getOnlinePlayers().forEach(player1 -> player1.showPlayer(player));
        }

        // TODO: You should definitely add more staff items. Some ideas could be; Random TP, MinerTP, FreezeBlock
        return true;
    }

    public boolean setStaffHidden(Player player, boolean s, boolean bypass) {
        // FIXME: I do not recommend running hide staff on an event. Mainly because its stupid and there's no point to make it run on an event as I don't see a reason why this would ever need to be cancelled.
        if (bypass) {
            // FIXME: This really doesn't need a bypass parameter as players should be able to run this at anytime they want
            if (s) {
                hideStaff.add(player);
                vanish.forEach(player::hidePlayer);
                player.sendMessage(StringUtils.format("&aStaff are now hidden."));
            } else {
                hideStaff.remove(player);
                vanish.forEach(player::showPlayer);
                player.sendMessage(StringUtils.format("&cStaff are now visible."));
            }
            return true;
        }
        if (s) {
            HideStaffEvent event = new HideStaffEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            hideStaff.add(player);
            vanish.forEach(player::hidePlayer);
        } else {
            HideStaffEvent event = new HideStaffEvent(player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            hideStaff.remove(player);
            vanish.forEach(player::showPlayer);
        }
        return true;

    }

    public void applyStaffItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassM = compass.getItemMeta();
        compassM.setDisplayName(StringUtils.format("&7* &cNavigator"));
        compass.setItemMeta(compassM);
        inventory.setItem(0, compass);

        ItemStack vanish;
        ItemMeta vanishM;
        if (isVanish(player)) {
            vanish = new ItemStack(Material.INK_SACK, 1, (short) 10);
            vanishM = vanish.getItemMeta();
            vanishM.setDisplayName(StringUtils.format("&7* &aHidden"));
        } else {
            vanish = new ItemStack(Material.INK_SACK, 1, (short) 8);
            vanishM = vanish.getItemMeta();
            vanishM.setDisplayName(StringUtils.format("&7* &cVisible"));
        }
        vanish.setItemMeta(vanishM);
        inventory.setItem(2, vanish);
    }

    public boolean inStaffModule(Player player) {
        return staffModule.contains(player);
    }

    public boolean isVanish(Player player) {
        return vanish.contains(player);
    }

    public boolean hasStaffHidden(Player player) {
        return hideStaff.contains(player);
    }
}
