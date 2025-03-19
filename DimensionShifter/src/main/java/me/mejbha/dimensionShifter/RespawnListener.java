package me.mejbha.dimensionShifter;

import me.mejbha.dimensionShifter.DimensionShifter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;

public class RespawnListener implements Listener {
    private final DimensionShifter plugin;

    public RespawnListener(DimensionShifter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Check if the player has a bed spawn
        Location bedSpawn = player.getRespawnLocation();
        if (bedSpawn != null) {
            event.setRespawnLocation(bedSpawn);
            return;
        }

        // If no bed spawn, get the default world from config.yml
        String defaultWorldName = plugin.getConfig().getString("default-respawn-world", "world");
        World defaultWorld = Bukkit.getWorld(defaultWorldName);

        if (defaultWorld != null) {
            Location spawnLocation = defaultWorld.getSpawnLocation();
            event.setRespawnLocation(spawnLocation);
        } else {
            plugin.getLogger().warning("Default respawn world '" + defaultWorldName + "' not found! Using fallback spawn.");
        }
    }
}