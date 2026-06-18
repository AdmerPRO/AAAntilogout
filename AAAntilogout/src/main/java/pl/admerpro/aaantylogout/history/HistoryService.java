package pl.admerpro.aaantylogout.history;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.combat.CombatResult;
import pl.admerpro.aaantylogout.config.PluginSettings;

public final class HistoryService {

    private final AAAntylogoutPlugin plugin;
    private final File file;
    private YamlConfiguration data;
    private PluginSettings settings;

    public HistoryService(AAAntylogoutPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.file = new File(plugin.getDataFolder(), "battle-history.yml");
        reload();
    }

    public void update(PluginSettings settings) {
        this.settings = settings;
    }

    public void record(BattleRecord record) {
        String path = path(record.playerId());
        List<Map<?, ?>> rawRecords = new ArrayList<>(data.getMapList(path));
        rawRecords.add(toMap(record));
        int maxRecords = settings.historyMaxRecordsPerPlayer();
        while (maxRecords > 0 && rawRecords.size() > maxRecords) {
            rawRecords.remove(0);
        }
        data.set(path, rawRecords);
        save();
    }

    public List<BattleRecord> getRecords(UUID playerId) {
        List<BattleRecord> records = new ArrayList<>();
        for (Map<?, ?> map : data.getMapList(path(playerId))) {
            fromMap(playerId, map).ifPresent(records::add);
        }
        Collections.reverse(records);
        return records;
    }

    public HistoryStats getStats(UUID playerId) {
        List<BattleRecord> records = getRecords(playerId);
        if (records.isEmpty()) {
            return new HistoryStats("-", 0, 0L, 0L, new EnumMap<>(CombatResult.class));
        }
        Map<CombatResult, Integer> counts = new EnumMap<>(CombatResult.class);
        long totalDuration = 0L;
        long longest = 0L;
        for (BattleRecord record : records) {
            counts.merge(record.result(), 1, Integer::sum);
            totalDuration += record.durationMillis();
            longest = Math.max(longest, record.durationMillis());
        }
        return new HistoryStats(records.get(0).playerName(), records.size(), totalDuration / records.size(), longest, counts);
    }

    public Optional<UUID> findPlayerId(String playerName) {
        String expected = playerName.toLowerCase(Locale.ROOT);
        if (!data.isConfigurationSection("players")) {
            return Optional.empty();
        }
        for (String key : data.getConfigurationSection("players").getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            for (BattleRecord record : getRecords(playerId)) {
                if (record.playerName().toLowerCase(Locale.ROOT).equals(expected)) {
                    return Optional.of(playerId);
                }
            }
        }
        return Optional.empty();
    }

    public void save() {
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder.");
            }
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save battle-history.yml.", exception);
        }
    }

    private void reload() {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    private String path(UUID playerId) {
        return "players." + playerId + ".records";
    }

    private Map<String, Object> toMap(BattleRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.id().toString());
        map.put("player-name", record.playerName());
        map.put("opponents", record.opponents());
        map.put("started-at", record.startedAt());
        map.put("ended-at", record.endedAt());
        map.put("duration-millis", record.durationMillis());
        map.put("result", record.result().configName());
        map.put("trigger-id", record.triggerId() == null ? "" : record.triggerId().toString());
        return map;
    }

    private Optional<BattleRecord> fromMap(UUID playerId, Map<?, ?> map) {
        try {
            UUID id = UUID.fromString(String.valueOf(valueOrDefault(map, "id", UUID.randomUUID().toString())));
            String playerName = String.valueOf(valueOrDefault(map, "player-name", "-"));
            List<String> opponents = new ArrayList<>();
            Object rawOpponents = map.get("opponents");
            if (rawOpponents instanceof List<?> list) {
                for (Object opponent : list) {
                    opponents.add(String.valueOf(opponent));
                }
            }
            long startedAt = asLong(map.get("started-at"));
            long endedAt = asLong(map.get("ended-at"));
            long durationMillis = asLong(map.get("duration-millis"));
            CombatResult result = CombatResult.fromConfigName(String.valueOf(valueOrDefault(map, "result", "timeout")));
            UUID triggerId = null;
            Object rawTrigger = map.get("trigger-id");
            if (rawTrigger != null && !String.valueOf(rawTrigger).isBlank()) {
                triggerId = UUID.fromString(String.valueOf(rawTrigger));
            }
            return Optional.of(new BattleRecord(id, playerId, playerName, opponents, startedAt, endedAt, durationMillis, result, triggerId));
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Skipped broken history record for " + playerId + ".");
            return Optional.empty();
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Object valueOrDefault(Map<?, ?> map, String key, Object defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : value;
    }
}
