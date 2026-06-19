package pl.admerpro.aaantylogout.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record PluginSettings(
    boolean enabled,
    int combatSeconds,
    boolean refreshTimerOnHit,
    boolean ignoreCreativeSpectator,
    Set<String> disabledWorlds,
    boolean killOnLogout,
    boolean dropItemsOnLogout,
    boolean clearInventoryOnLogout,
    boolean broadcastLogoutPunish,
    boolean bossBarEnabled,
    boolean actionBarEnabled,
    boolean chatReminderEnabled,
    int chatReminderIntervalSeconds,
    boolean recordServerStop,
    CommandMode commandMode,
    Set<String> commandList,
    Set<Material> blockedItems,
    Set<PlayerTeleportEvent.TeleportCause> blockedTeleportCauses,
    boolean blockAllTeleportCauses,
    boolean blockItemDrop,
    boolean blockElytra,
    boolean worldGuardEnabled,
    boolean restrictRegionsInCombat,
    boolean preventTaggingInBlockedRegions,
    Set<String> blockedRegionNames,
    Map<String, LocalRegion> localRegions,
    int historyMaxRecordsPerPlayer,
    int historyPageSize,
    Map<String, String> messages
) {

    public long combatMillis() {
        return combatSeconds * 1000L;
    }

    public boolean isCommandAllowed(String command) {
        String normalized = normalizeCommand(command);
        if (commandMode == CommandMode.WHITELIST) {
            return commandList.contains(normalized);
        }
        return !commandList.contains(normalized);
    }

    public boolean isBlockedItem(Material material) {
        return blockedItems.contains(material);
    }

    public boolean isTeleportCauseBlocked(PlayerTeleportEvent.TeleportCause cause) {
        return blockAllTeleportCauses || blockedTeleportCauses.contains(cause);
    }

    public static PluginSettings load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        Set<Material> blockedItems = loadMaterials(plugin, config.getStringList("restrictions.items.blocked"));
        TeleportCauseSettings teleportCauseSettings = loadTeleportCauses(plugin, config.getStringList("restrictions.teleport.blocked-causes"));
        return new PluginSettings(
            config.getBoolean("combat.enabled", true),
            Math.max(1, config.getInt("combat.duration-seconds", 20)),
            config.getBoolean("combat.refresh-timer-on-hit", true),
            config.getBoolean("combat.ignore-creative-spectator", false),
            lowerSet(config.getStringList("combat.disabled-worlds")),
            config.getBoolean("punishments.logout.kill", true),
            config.getBoolean("punishments.logout.drop-items", true),
            config.getBoolean("punishments.logout.clear-inventory", true),
            config.getBoolean("punishments.logout.broadcast", true),
            config.getBoolean("visuals.bossbar.enabled", true),
            config.getBoolean("visuals.actionbar.enabled", true),
            config.getBoolean("visuals.chat-reminder.enabled", true),
            Math.max(1, config.getInt("visuals.chat-reminder.interval-seconds", 5)),
            config.getBoolean("history.record-server-stop", true),
            CommandMode.from(config.getString("restrictions.commands.mode", "BLACKLIST")),
            normalizeCommands(config.getStringList("restrictions.commands.list")),
            blockedItems,
            teleportCauseSettings.causes(),
            teleportCauseSettings.all(),
            config.getBoolean("restrictions.item-drop.block", false),
            config.getBoolean("restrictions.elytra.block", true),
            config.getBoolean("regions.worldguard.enabled", true),
            config.getBoolean("regions.block-in-combat", true),
            config.getBoolean("regions.prevent-tagging-inside-blocked-region", false),
            lowerSet(config.getStringList("regions.blocked")),
            loadLocalRegions(config),
            Math.max(0, config.getInt("history.max-records-per-player", 5000)),
            Math.max(1, config.getInt("history.page-size", 8)),
            loadMessages(config)
        );
    }

    private static Set<Material> loadMaterials(JavaPlugin plugin, List<String> values) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String value : values) {
            Material material = Material.matchMaterial(value);
            if (material == null) {
                plugin.getLogger().warning("Unknown material in restrictions.items.blocked: " + value);
                continue;
            }
            materials.add(material);
        }
        return Collections.unmodifiableSet(materials);
    }

    private static TeleportCauseSettings loadTeleportCauses(JavaPlugin plugin, List<String> values) {
        Set<PlayerTeleportEvent.TeleportCause> causes = EnumSet.noneOf(PlayerTeleportEvent.TeleportCause.class);
        boolean all = false;
        for (String value : values) {
            if (value.equalsIgnoreCase("ALL")) {
                all = true;
                continue;
            }
            try {
                causes.add(PlayerTeleportEvent.TeleportCause.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Unknown teleport cause in restrictions.teleport.blocked-causes: " + value);
            }
        }
        return new TeleportCauseSettings(Collections.unmodifiableSet(causes), all);
    }

    private static Set<String> lowerSet(List<String> values) {
        Set<String> result = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static Set<String> normalizeCommands(List<String> commands) {
        Set<String> result = new HashSet<>();
        for (String command : commands) {
            result.add(normalizeCommand(command));
        }
        return Collections.unmodifiableSet(result);
    }

    private static String normalizeCommand(String command) {
        String normalized = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    private static Map<String, LocalRegion> loadLocalRegions(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("regions.local-regions");
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, LocalRegion> regions = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String path = "regions.local-regions." + key;
            String world = config.getString(path + ".world", "world");
            List<Integer> min = new ArrayList<>(config.getIntegerList(path + ".min"));
            List<Integer> max = new ArrayList<>(config.getIntegerList(path + ".max"));
            if (min.size() < 3 || max.size() < 3) {
                continue;
            }
            int minX = Math.min(min.get(0), max.get(0));
            int minY = Math.min(min.get(1), max.get(1));
            int minZ = Math.min(min.get(2), max.get(2));
            int maxX = Math.max(min.get(0), max.get(0));
            int maxY = Math.max(min.get(1), max.get(1));
            int maxZ = Math.max(min.get(2), max.get(2));
            regions.put(key.toLowerCase(Locale.ROOT), new LocalRegion(key, world, minX, minY, minZ, maxX, maxY, maxZ));
        }
        return Collections.unmodifiableMap(regions);
    }

    private static Map<String, String> loadMessages(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String key : section.getKeys(false)) {
            result.put(key, section.getString(key, key));
        }
        return Collections.unmodifiableMap(result);
    }

    public enum CommandMode {
        BLACKLIST,
        WHITELIST;

        public static CommandMode from(String value) {
            try {
                return CommandMode.valueOf(value == null ? "BLACKLIST" : value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return BLACKLIST;
            }
        }
    }

    private record TeleportCauseSettings(Set<PlayerTeleportEvent.TeleportCause> causes, boolean all) {
    }
}
