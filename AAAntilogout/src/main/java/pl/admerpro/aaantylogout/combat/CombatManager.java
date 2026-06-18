package pl.admerpro.aaantylogout.combat;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.config.PluginSettings;
import pl.admerpro.aaantylogout.history.BattleRecord;
import pl.admerpro.aaantylogout.history.HistoryService;
import pl.admerpro.aaantylogout.region.RegionService;
import pl.admerpro.aaantylogout.util.MessageService;
import pl.admerpro.aaantylogout.util.Permissions;
import pl.admerpro.aaantylogout.util.TimeFormatter;

public final class CombatManager {

    private final AAAntylogoutPlugin plugin;
    private final MessageService messages;
    private final HistoryService historyService;
    private final RegionService regionService;
    private final ConcurrentMap<UUID, CombatSession> sessions = new ConcurrentHashMap<>();
    private PluginSettings settings;
    private BukkitTask task;

    public CombatManager(AAAntylogoutPlugin plugin, PluginSettings settings, MessageService messages, HistoryService historyService, RegionService regionService) {
        this.plugin = plugin;
        this.settings = settings;
        this.messages = messages;
        this.historyService = historyService;
        this.regionService = regionService;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (settings.recordServerStop()) {
            for (UUID playerId : Set.copyOf(sessions.keySet())) {
                finishSession(playerId, CombatResult.SERVER_STOP, null, false);
            }
        } else {
            sessions.values().forEach(CombatSession::removeBossBar);
            sessions.clear();
        }
    }

    public void update(PluginSettings settings) {
        this.settings = settings;
        sessions.values().forEach(session -> {
            if (!settings.bossBarEnabled()) {
                session.removeBossBar();
            }
        });
    }

    public void tag(Player attacker, Player victim) {
        if (!canTag(attacker, victim)) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean attackerNew = !sessions.containsKey(attacker.getUniqueId());
        boolean victimNew = !sessions.containsKey(victim.getUniqueId());

        CombatSession attackerSession = sessions.computeIfAbsent(attacker.getUniqueId(), ignored -> new CombatSession(attacker, now));
        CombatSession victimSession = sessions.computeIfAbsent(victim.getUniqueId(), ignored -> new CombatSession(victim, now));
        attackerSession.touch(attacker, victim, now, settings.refreshTimerOnHit());
        victimSession.touch(victim, attacker, now, settings.refreshTimerOnHit());

        if (attackerNew) {
            messages.send(attacker, "combat-start", "opponent", victim.getName(), "seconds", String.valueOf(settings.combatSeconds()));
        }
        if (victimNew) {
            messages.send(victim, "combat-start", "opponent", attacker.getName(), "seconds", String.valueOf(settings.combatSeconds()));
        }
        updateIndicators(attackerSession);
        updateIndicators(victimSession);
    }

    public boolean isInCombat(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public Optional<CombatSession> getSession(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public long getRemainingMillis(UUID playerId) {
        CombatSession session = sessions.get(playerId);
        if (session == null) {
            return 0L;
        }
        return Math.max(0L, session.remainingMillis(System.currentTimeMillis(), settings.combatMillis()));
    }

    public void finishGroup(Player trigger, CombatResult result) {
        CombatSession session = sessions.get(trigger.getUniqueId());
        if (session == null) {
            return;
        }
        Set<UUID> participants = new LinkedHashSet<>();
        participants.add(trigger.getUniqueId());
        participants.addAll(session.opponents().keySet());
        for (UUID participant : participants) {
            finishSession(participant, result, trigger.getUniqueId(), true);
        }
    }

    private boolean canTag(Player attacker, Player victim) {
        if (!settings.enabled() || attacker.equals(victim)) {
            return false;
        }
        if (attacker.hasPermission(Permissions.BYPASS) || victim.hasPermission(Permissions.BYPASS)) {
            return false;
        }
        if (settings.disabledWorlds().contains(attacker.getWorld().getName().toLowerCase()) || settings.disabledWorlds().contains(victim.getWorld().getName().toLowerCase())) {
            return false;
        }
        if (settings.ignoreCreativeSpectator() && (isCreativeLike(attacker) || isCreativeLike(victim))) {
            return false;
        }
        if (settings.preventTaggingInBlockedRegions() && (regionService.isBlocked(attacker.getLocation()) || regionService.isBlocked(victim.getLocation()))) {
            return false;
        }
        return !attacker.isDead() && !victim.isDead();
    }

    private boolean isCreativeLike(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (UUID playerId : Set.copyOf(sessions.keySet())) {
            CombatSession session = sessions.get(playerId);
            if (session == null) {
                continue;
            }
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            if (session.remainingMillis(now, settings.combatMillis()) <= 0L) {
                finishSession(playerId, CombatResult.TIMEOUT, null, true);
            } else {
                updateIndicators(session);
            }
        }
    }

    private void updateIndicators(CombatSession session) {
        if (!settings.bossBarEnabled()) {
            return;
        }
        Player player = Bukkit.getPlayer(session.playerId());
        if (player == null) {
            return;
        }
        BossBar bossBar = session.bossBar();
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
            session.setBossBar(bossBar);
        }
        long remaining = getRemainingMillis(session.playerId());
        double progress = settings.combatMillis() <= 0L ? 0.0D : remaining / (double) settings.combatMillis();
        bossBar.setProgress(Math.max(0.0D, Math.min(1.0D, progress)));
        bossBar.setTitle(messages.format("bossbar-title",
            "time", TimeFormatter.formatDuration(remaining),
            "opponents", session.opponentNamesText()));
        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    private void finishSession(UUID playerId, CombatResult result, UUID triggerId, boolean notify) {
        CombatSession session = sessions.remove(playerId);
        if (session == null) {
            return;
        }
        session.removeBossBar();
        long endedAt = System.currentTimeMillis();
        historyService.record(BattleRecord.fromSession(session, result, triggerId, endedAt));

        Player player = Bukkit.getPlayer(playerId);
        if (notify && player != null) {
            messages.send(player, "combat-end", "result", result.configName());
        }
    }
}
