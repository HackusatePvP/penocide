package dev.hcr.penocide.handler;

import dev.hcr.penocide.Penocide;
import dev.hcr.penocide.events.HideStaffEvent;
import dev.hcr.penocide.events.StaffModuleEnterEvent;
import dev.hcr.penocide.events.StaffModuleLeaveEvent;
import dev.hcr.penocide.events.StaffVanishEvent;
import dev.hcr.penocide.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class StaffListener implements Listener {
    private final Penocide plugin;

    public StaffListener(Penocide plugin) {
        this.plugin = plugin;
    }

    // The first 4 event priorities should be MONITOR.
    // This is because MONITOR is called last and we need
    // to check if the event is cancelled before sending
    // the player messages.

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanish(StaffVanishEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }
        // You may realize the ternary is setup opposite, this is because the event is called before setting the vanish state. This means when this event is called the players vanish state hasn't been updated.
        player.sendMessage(StringUtils.format((plugin.getStaffManager().isVanish(player) ? "&cYou are now visible." : "&aYou are now hidden.")));
        Bukkit.getOnlinePlayers().forEach(player1 -> player1.hidePlayer(player));
        Bukkit.getOnlinePlayers().stream().filter(player1 -> player1.hasPermission("penoicide.staff")).filter(player1 -> !plugin.getStaffManager().getHideStaff().contains(player1)).forEach(player1 -> player1.showPlayer(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStaffModuleEnter(StaffModuleEnterEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }
        // Way easier to setup configurable messages in events than having a 100 line command lol.
        player.sendMessage(StringUtils.format("&aYou have entered the staff module."));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStaffModuleLeave(StaffModuleLeaveEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }
        player.sendMessage(StringUtils.format("&cYou have left the staff module."));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHideStaff(HideStaffEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) {
            return;
        }
        player.sendMessage(StringUtils.format((plugin.getStaffManager().hasStaffHidden(player) ? "&cStaff are now visible." : "&aStaff are now hidden.")));
    }

    @EventHandler(priority = EventPriority.LOW) //FIXME: I highly recommend removing this as this is just to show how cancelling an event works
    public void onStaffMode(StaffModuleEnterEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().getCooldown().get(player) > System.currentTimeMillis()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStaffManager().getVanish().forEach(player::hidePlayer);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().inStaffModule(player)) {
            plugin.getStaffManager().setStaffModule(player, false, true);
        }
        plugin.getStaffManager().getVanish().forEach(player::showPlayer);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != null) {
            Player player = (Player) event.getWhoClicked();
            if (plugin.getStaffManager().inStaffModule(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().inStaffModule(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().inStaffModule(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler

    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().inStaffModule(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStaffManager().inStaffModule(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem().getItemMeta().getDisplayName().contains("Hidden")) {
                plugin.getStaffManager().setVanish(player, false, true);
            }
            if (event.getItem().getItemMeta().getDisplayName().contains("Visible")) {
                plugin.getStaffManager().setVanish(player, true, true);
            }
            plugin.getStaffManager().applyStaffItems(player);
        }
    }

    // I'm going to comment this about but here's a sample code on how to add a freeze staff item
    //@EventHandler
    //public void onEntityFreeze(PlayerInteractAtEntityEvent event) {
    //    Player player = event.getPlayer();
    //     if (plugin.getStaffManager().inStaffModule(player)) {
    //        if (event.getRightClicked() instanceof Player) {
    //            if (event.getPlayer().getItemInHand() != null) {
    //                ItemStack itemStack = player.getItemInHand();
    //                if (itemStack.getItemMeta().getDisplayName().contains("Freeze")) {
    //                    // TODO: Insert code here
    //                }
    //            }
    //        }
    //    }
    //}

    @EventHandler //FIXME: I highly recommend removing this as this is just to show how cancelling an event works
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            plugin.getStaffManager().getCooldown().put(damager, System.currentTimeMillis() + 3000);
            plugin.getStaffManager().getCooldown().put(player, System.currentTimeMillis() + 3000);
            damager.sendMessage("Test");
            player.sendMessage("Test");
        }
    }

}
