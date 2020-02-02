package de.Zorro909.ConfigurationLibrary.Serializers.Minecraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import de.Zorro909.ConfigurationLibrary.ConfigurationPane;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;

/**
 * 
 * @author Zorro909
 *
 */
public class LocationSerializer implements Serializer<Location> {

    @Override
    public void serialize(Location object, ConfigurationPane configPane) {
        configPane.set("world", object.getWorld().getName());
        configPane.set("x", object.getX());
        configPane.set("y", object.getY());
        configPane.set("z", object.getZ());
        configPane.set("yaw", object.getYaw());
        configPane.set("pitch", object.getPitch());
    }

    @Override
    public Location deserialize(ConfigurationPane configPane) {
        String world = configPane.getString("world");
        double x = configPane.get("x", Double.class);
        double y = configPane.get("y", Double.class);
        double z = configPane.get("z", Double.class);
        Float yaw = configPane.get("yaw", Float.class);
        Float pitch = configPane.get("pitch", Float.class);
        if (yaw != null && pitch != null) {
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public Class<Location> getSerializedType() {
        return Location.class;
    }

}
