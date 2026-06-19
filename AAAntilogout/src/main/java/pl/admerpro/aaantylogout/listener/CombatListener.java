package pl.admerpro.aaantylogout.listener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.combat.CombatManager;
import pl.admerpro.aaantylogout.combat.CombatResult;
import pl.admerpro.aaantylogout.util.MessageService;

public final class CombatListener implements Listener {

    private final AAAntylogoutPlugin plugin;
    private final CombatManager combatManager;
    private final MessageService messages;
    private final Set<UUID> pendingJoinDeaths = ConcurrentHashMap.newKeySet();

    public CombatListener(AAAntylogoutPlugin plugin, CombatManager combatManager, MessageService messages) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = findAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }
        combatManager.tag(attacker, victim);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.settings().keepTimerAfterOpponentDeath()) {
            combatManager.finishPlayer(event.getEntity(), CombatResult.DEATH);
            return;
        }
        combatManager.finishGroup(event.getEntity(), CombatResult.DEATH);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player.getUniqueId())) {
            return;
        }
        punishLogout(player);
        combatManager.finishGroup(player, CombatResult.LOGOUT);
        if (plugin.settings().broadcastLogoutPunish()) {
            plugin.getServer().broadcastMessage(messages.format("logout-broadcast", "player", player.getName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!pendingJoinDeaths.remove(player.getUniqueId())) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && !player.isDead() && plugin.settings().killOnLogout()) {
                player.setHealth(0.0D);
            }
        });
    }

    private Player findAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private void punishLogout(Player player) {
        if (plugin.settings().dropItemsOnLogout()) {
            dropInventory(player);
        }
        if (plugin.settings().clearInventoryOnLogout()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
        }
        if (!plugin.settings().killOnLogout()) {
            return;
        }
        pendingJoinDeaths.add(player.getUniqueId());
        if (!player.isDead()) {
            try {
                player.setHealth(0.0D);
            } catch (IllegalArgumentException exception) {
                player.damage(1000.0D);
            }
        }
    }

    private void dropInventory(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        dropItems(world, location, player.getInventory().getStorageContents());
        dropItems(world, location, player.getInventory().getArmorContents());
        dropItems(world, location, player.getInventory().getExtraContents());
    }

    private void dropItems(World world, Location location, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            world.dropItemNaturally(location, item.clone());
        }
    }
}
