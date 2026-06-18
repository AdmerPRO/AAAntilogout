package pl.admerpro.aaantylogout.history;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import pl.admerpro.aaantylogout.combat.CombatResult;
import pl.admerpro.aaantylogout.combat.CombatSession;

public record BattleRecord(
    UUID id,
    UUID playerId,
    String playerName,
    List<String> opponents,
    long startedAt,
    long endedAt,
    long durationMillis,
    CombatResult result,
    UUID triggerId
) {

    public static BattleRecord fromSession(CombatSession session, CombatResult result, UUID triggerId, long endedAt) {
        return new BattleRecord(
            UUID.randomUUID(),
            session.playerId(),
            session.playerName(),
            new ArrayList<>(session.opponents().values()),
            session.startedAt(),
            endedAt,
            Math.max(0L, endedAt - session.startedAt()),
            result,
            triggerId
        );
    }

    public String opponentsText() {
        if (opponents.isEmpty()) {
            return "-";
        }
        return opponents.stream().collect(Collectors.joining(", "));
    }
}
