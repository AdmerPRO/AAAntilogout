package pl.admerpro.aaantylogout.listener;

import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import pl.admerpro.aaantylogout.combat.CombatManager;
import pl.admerpro.aaantylogout.region.RegionService;
import pl.admerpro.aaantylogout.util.MessageService;
import pl.admerpro.aaantylogout.util.Permissions;

public final class RestrictionListener implements Listener {

    private final CombatManager combatManager;
    private final RegionService regionService;
    private final MessageService messages;

    public RestrictionListener(CombatManager combatManager, RegionService regionService, MessageService messages) {
        this.combatManager = combatManager;
        this.regionService = regionService;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!isRestricted(player)) {
            return;
        }
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase(Locale.ROOT);
        if (player.hasPermission(Permissions.COMMAND_BYPASS)) {
            return;
        }
        if (!messages.settings().isCommandAllowed(command)) {
            event.setCancelled(true);
            messages.send(player, "command-blocked", "command", command);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isRestricted(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (isBlockedItem(item)) {
            event.setCancelled(true);
            messages.send(player, "item-blocked", "item", item.getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!isRestricted(player) || !isBlockedItem(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        messages.send(player, "item-blocked", "item", event.getItem().getType().name());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!isRestricted(player) || !messages.settings().isTeleportCauseBlocked(event.getCause())) {
            return;
        }
        event.setCancelled(true);
        messages.send(player, "teleport-blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isRestricted(player) || !messages.settings().blockItemDrop()) {
            return;
        }
        event.setCancelled(true);
        messages.send(player, "drop-blocked");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!messages.settings().restrictRegionsInCombat() || !isRestricted(player)) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || sameBlock(from, to)) {
            return;
        }
        if (!regionService.isBlocked(from) && regionService.isBlocked(to)) {
            event.setCancelled(true);
            messages.send(player, "region-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player) || !event.isGliding()) {
            return;
        }
        if (!isRestricted(player) || !messages.settings().blockElytra()) {
            return;
        }
        event.setCancelled(true);
        messages.send(player, "elytra-blocked");
    }

    private boolean isRestricted(Player player) {
        return combatManager.isInCombat(player.getUniqueId()) && !player.hasPermission(Permissions.BYPASS);
    }

    private boolean isBlockedItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR && messages.settings().isBlockedItem(item.getType());
    }

    private boolean sameBlock(Location first, Location second) {
        return first.getWorld() == second.getWorld()
            && first.getBlockX() == second.getBlockX()
            && first.getBlockY() == second.getBlockY()
            && first.getBlockZ() == second.getBlockZ();
    }
}
