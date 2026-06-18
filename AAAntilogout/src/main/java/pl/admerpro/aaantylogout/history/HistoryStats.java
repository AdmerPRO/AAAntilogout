package pl.admerpro.aaantylogout.history;

import java.util.Map;
import pl.admerpro.aaantylogout.combat.CombatResult;

public record HistoryStats(String playerName, int total, long averageDurationMillis, long longestDurationMillis, Map<CombatResult, Integer> counts) {

    public int count(CombatResult result) {
        return counts.getOrDefault(result, 0);
    }
}
