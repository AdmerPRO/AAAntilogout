package pl.admerpro.aaantylogout.util;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.config.PluginSettings;

public final class MessageService {

    private final AAAntylogoutPlugin plugin;
    private PluginSettings settings;

    public MessageService(AAAntylogoutPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void update(PluginSettings settings) {
        this.settings = settings;
    }

    public PluginSettings settings() {
        return settings;
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(format(key, placeholders));
    }

    public String format(String key, String... placeholders) {
        String message = settings.messages().getOrDefault(key, key);
        message = message.replace("{prefix}", settings.messages().getOrDefault("prefix", ""));
        Map<String, String> replacements = PlaceholderMap.of(placeholders);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return color(message);
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
