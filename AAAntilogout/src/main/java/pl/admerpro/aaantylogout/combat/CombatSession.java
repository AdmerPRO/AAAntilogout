package pl.admerpro.aaantylogout.combat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public final class CombatSession {

    private final UUID playerId;
    private String playerName;
    private final long startedAt;
    private long lastTaggedAt;
    private long lastChatReminderAt;
    private final Map<UUID, String> opponents = new LinkedHashMap<>();
    private BossBar bossBar;

    public CombatSession(Player player, long now) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.startedAt = now;
        this.lastTaggedAt = now;
        this.lastChatReminderAt = now;
    }

    public void touch(Player player, Player opponent, long now, boolean refreshTimer) {
        this.playerName = player.getName();
        this.opponents.put(opponent.getUniqueId(), opponent.getName());
        if (refreshTimer) {
            this.lastTaggedAt = now;
        }
    }

    public long remainingMillis(long now, long combatMillis) {
        return combatMillis - (now - lastTaggedAt);
    }

    public String opponentNamesText() {
        if (opponents.isEmpty()) {
            return "-";
        }
        return opponents.values().stream().collect(Collectors.joining(", "));
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    public UUID playerId() {
        return playerId;
    }

    public String playerName() {
        return playerName;
    }

    public long startedAt() {
        return startedAt;
    }

    public long lastTaggedAt() {
        return lastTaggedAt;
    }

    public long lastChatReminderAt() {
        return lastChatReminderAt;
    }

    public void markChatReminder(long now) {
        this.lastChatReminderAt = now;
    }

    public Map<UUID, String> opponents() {
        return opponents;
    }

    public BossBar bossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }
}
