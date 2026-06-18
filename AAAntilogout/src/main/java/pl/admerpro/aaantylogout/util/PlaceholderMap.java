package pl.admerpro.aaantylogout.util;

import java.util.HashMap;
import java.util.Map;

public final class PlaceholderMap {

    private PlaceholderMap() {
    }

    public static Map<String, String> of(String... placeholders) {
        Map<String, String> values = new HashMap<>();
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            values.put(placeholders[index], placeholders[index + 1]);
        }
        return values;
    }
}
