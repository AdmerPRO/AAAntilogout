package pl.admerpro.aaantylogout.region;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import pl.admerpro.aaantylogout.AAAntylogoutPlugin;

public final class WorldGuardRegionHook {

    private final AAAntylogoutPlugin plugin;
    private boolean available;
    private boolean warningSent;
    private Object query;
    private Method adaptLocationMethod;
    private Method getApplicableRegionsMethod;

    public WorldGuardRegionHook(AAAntylogoutPlugin plugin, boolean enabled) {
        this.plugin = plugin;
        this.available = enabled && initialize();
    }

    public boolean available() {
        return available;
    }

    public Set<String> regionIds(Location location) {
        if (!available) {
            return Collections.emptySet();
        }
        try {
            Object adaptedLocation = adaptLocationMethod.invoke(null, location);
            Object applicableRegions = getApplicableRegionsMethod.invoke(query, adaptedLocation);
            Method getRegionsMethod = applicableRegions.getClass().getMethod("getRegions");
            Object rawRegions = getRegionsMethod.invoke(applicableRegions);
            if (!(rawRegions instanceof Iterable<?> regions)) {
                return Collections.emptySet();
            }
            Set<String> ids = new HashSet<>();
            for (Object region : regions) {
                Method getIdMethod = region.getClass().getMethod("getId");
                ids.add(String.valueOf(getIdMethod.invoke(region)));
            }
            return ids;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            available = false;
            warnOnce("WorldGuard region lookup failed. Region protection has been disabled until reload.", exception);
            return Collections.emptySet();
        }
    }

    private boolean initialize() {
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !worldGuard.isEnabled()) {
            plugin.getLogger().info("WorldGuard was not found. Local regions will still work.");
            return false;
        }
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object instance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = instance.getClass().getMethod("getPlatform").invoke(instance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Class<?> worldEditLocationClass = Class.forName("com.sk89q.worldedit.util.Location");
            adaptLocationMethod = bukkitAdapterClass.getMethod("adapt", Location.class);
            getApplicableRegionsMethod = query.getClass().getMethod("getApplicableRegions", worldEditLocationClass);
            plugin.getLogger().info("WorldGuard support enabled.");
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnOnce("WorldGuard was found, but the hook could not be initialized.", exception);
            return false;
        }
    }

    private void warnOnce(String message, Exception exception) {
        if (warningSent) {
            return;
        }
        warningSent = true;
        plugin.getLogger().log(Level.WARNING, message, exception);
    }
}
