package pl.admerpro.aaantylogout.region;

import java.util.Locale;
import org.bukkit.Location;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;
import pl.admerpro.aaantylogout.config.LocalRegion;
import pl.admerpro.aaantylogout.config.PluginSettings;

public final class RegionService {

    private final AAAntylogoutPlugin plugin;
    private PluginSettings settings;
    private WorldGuardRegionHook worldGuardHook;

    public RegionService(AAAntylogoutPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        update(settings);
    }

    public void update(PluginSettings settings) {
        this.settings = settings;
        this.worldGuardHook = new WorldGuardRegionHook(plugin, settings.worldGuardEnabled());
    }

    public boolean isBlocked(Location location) {
        if (location == null || settings.blockedRegionNames().isEmpty()) {
            return false;
        }
        for (LocalRegion region : settings.localRegions().values()) {
            if (settings.blockedRegionNames().contains(region.name().toLowerCase(Locale.ROOT)) && region.contains(location)) {
                return true;
            }
        }
        if (!settings.worldGuardEnabled() || !worldGuardHook.available()) {
            return false;
        }
        return worldGuardHook.regionIds(location).stream()
            .map(region -> region.toLowerCase(Locale.ROOT))
            .anyMatch(settings.blockedRegionNames()::contains);
    }
}
