package pl.admerpro.aaantylogout.config;

import org.bukkit.Location;

public record LocalRegion(String name, String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
