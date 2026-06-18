package pl.admerpro.aaantylogout.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.combat.CombatResult;
import pl.admerpro.aaantylogout.combat.CombatSession;
import pl.admerpro.aaantylogout.history.BattleRecord;
import pl.admerpro.aaantylogout.history.HistoryStats;
import pl.admerpro.aaantylogout.util.Permissions;
import pl.admerpro.aaantylogout.util.TimeFormatter;

public final class AntiLogoutCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("help", "reload", "status", "history", "stats", "end");
    private final AAAntylogoutPlugin plugin;

    public AntiLogoutCommand(AAAntylogoutPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN)) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "reload" -> reload(sender);
            case "status" -> status(sender, args);
            case "history" -> history(sender, args);
            case "stats" -> stats(sender, args);
            case "end" -> end(sender, args);
            default -> plugin.messages().send(sender, "unknown-command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && List.of("status", "history", "stats", "end").contains(args[0].toLowerCase(Locale.ROOT))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().collect(Collectors.toList()), args[1]);
        }
        return Collections.emptyList();
    }

    private void reload(CommandSender sender) {
        plugin.reloadPlugin();
        plugin.messages().send(sender, "reload");
    }

    private void status(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(sender, "usage-status");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-online");
            return;
        }
        Optional<CombatSession> session = plugin.combatManager().getSession(target.getUniqueId());
        if (session.isEmpty()) {
            plugin.messages().send(sender, "status-safe", "player", target.getName());
            return;
        }

        long remaining = plugin.combatManager().getRemainingMillis(target.getUniqueId());
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
        sender.sendMessage(plugin.messages().color("&cAAAntylogout &8| &fPlayer status &c" + target.getName()));
        sender.sendMessage(plugin.messages().color("&7Remaining: &f" + TimeFormatter.formatDuration(remaining)));
        sender.sendMessage(plugin.messages().color("&7Opponents: &f" + session.get().opponentNamesText()));
        sender.sendMessage(plugin.messages().color("&7Start: &f" + TimeFormatter.formatDate(session.get().startedAt())));
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
    }

    private void history(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(sender, "usage-history");
            return;
        }
        Optional<UUID> playerId = findPlayerId(args[1]);
        if (playerId.isEmpty()) {
            plugin.messages().send(sender, "history-empty", "player", args[1]);
            return;
        }

        int page = parsePage(args);
        List<BattleRecord> records = plugin.historyService().getRecords(playerId.get());
        if (records.isEmpty()) {
            plugin.messages().send(sender, "history-empty", "player", args[1]);
            return;
        }

        int pageSize = Math.max(1, plugin.settings().historyPageSize());
        int pages = Math.max(1, (int) Math.ceil(records.size() / (double) pageSize));
        page = Math.min(Math.max(page, 1), pages);
        int from = (page - 1) * pageSize;
        int to = Math.min(records.size(), from + pageSize);

        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
        sender.sendMessage(plugin.messages().color("&cAAAntylogout &8| &fBattle history for &c" + records.get(0).playerName() + " &7(" + page + "/" + pages + ")"));
        for (BattleRecord record : records.subList(from, to)) {
            sender.sendMessage(plugin.messages().color("&7- &f" + TimeFormatter.formatDate(record.endedAt())
                + " &8| &c" + record.result().configName()
                + " &8| &7duration: &f" + TimeFormatter.formatDuration(record.durationMillis())
                + " &8| &7against: &f" + record.opponentsText()));
        }
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
    }

    private void stats(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(sender, "usage-stats");
            return;
        }
        Optional<UUID> playerId = findPlayerId(args[1]);
        if (playerId.isEmpty()) {
            plugin.messages().send(sender, "history-empty", "player", args[1]);
            return;
        }
        HistoryStats stats = plugin.historyService().getStats(playerId.get());
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
        sender.sendMessage(plugin.messages().color("&cAAAntylogout &8| &fBattle stats for &c" + stats.playerName()));
        sender.sendMessage(plugin.messages().color("&7Total battles: &f" + stats.total()));
        sender.sendMessage(plugin.messages().color("&7Timeout: &f" + stats.count(CombatResult.TIMEOUT)));
        sender.sendMessage(plugin.messages().color("&7Death: &f" + stats.count(CombatResult.DEATH)));
        sender.sendMessage(plugin.messages().color("&7Logout: &f" + stats.count(CombatResult.LOGOUT)));
        sender.sendMessage(plugin.messages().color("&7Admin/server: &f" + (stats.count(CombatResult.ADMIN) + stats.count(CombatResult.SERVER_STOP))));
        sender.sendMessage(plugin.messages().color("&7Average battle duration: &f" + TimeFormatter.formatDuration(stats.averageDurationMillis())));
        sender.sendMessage(plugin.messages().color("&7Longest battle: &f" + TimeFormatter.formatDuration(stats.longestDurationMillis())));
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
    }

    private void end(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(sender, "usage-end");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "player-not-online");
            return;
        }
        if (!plugin.combatManager().isInCombat(target.getUniqueId())) {
            plugin.messages().send(sender, "status-safe", "player", target.getName());
            return;
        }
        plugin.combatManager().finishGroup(target, CombatResult.ADMIN);
        plugin.messages().send(sender, "combat-ended-admin", "player", target.getName());
    }

    private Optional<UUID> findPlayerId(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return Optional.of(online.getUniqueId());
        }
        Optional<UUID> fromHistory = plugin.historyService().findPlayerId(name);
        if (fromHistory.isPresent()) {
            return fromHistory;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.hasPlayedBefore() ? Optional.of(offline.getUniqueId()) : Optional.empty();
    }

    private int parsePage(String[] args) {
        if (args.length < 3) {
            return 1;
        }
        try {
            return Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
        sender.sendMessage(plugin.messages().color("&cAAAntylogout &8| &fAdministration commands"));
        sender.sendMessage(plugin.messages().color("&7/" + label + " reload &8- &freloads the configuration"));
        sender.sendMessage(plugin.messages().color("&7/" + label + " status <player> &8- &fshows active combat"));
        sender.sendMessage(plugin.messages().color("&7/" + label + " history <player> [page] &8- &fshows battle history"));
        sender.sendMessage(plugin.messages().color("&7/" + label + " stats <player> &8- &fshows battle statistics"));
        sender.sendMessage(plugin.messages().color("&7/" + label + " end <player> &8- &fends a player's combat"));
        sender.sendMessage(plugin.messages().color("&8&m--------------------------------------------------"));
    }

    private List<String> filter(List<String> values, String typed) {
        String lowerTyped = typed.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerTyped))
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
