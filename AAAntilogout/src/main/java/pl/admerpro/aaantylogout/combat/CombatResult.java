package pl.admerpro.aaantylogout.combat;

import java.util.Locale;

public enum CombatResult {
    TIMEOUT("timeout"),
    DEATH("death"),
    LOGOUT("logout"),
    ADMIN("admin"),
    SERVER_STOP("server_stop");

    private final String configName;

    CombatResult(String configName) {
        this.configName = configName;
    }

    public String configName() {
        return configName;
    }

    public static CombatResult fromConfigName(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (CombatResult result : values()) {
            if (result.configName.equals(normalized)) {
                return result;
            }
        }
        return TIMEOUT;
    }
}
