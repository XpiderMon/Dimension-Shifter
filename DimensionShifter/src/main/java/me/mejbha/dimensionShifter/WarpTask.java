package me.mejbha.dimensionShifter;

import com.earth2me.essentials.Essentials;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarpTask extends BukkitRunnable {
    private final DimensionShifter plugin;
    private final Random random = new Random();
    private Essentials essentials;

    public WarpTask(DimensionShifter plugin) {
        this.plugin = plugin;

        // Check if EssentialsX is installed
        Plugin essPlugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essPlugin instanceof Essentials) {
            essentials = (Essentials) essPlugin;
        }
    }

    @Override
    public void run() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return;

        int chance = plugin.getConfig().getInt("warp-chance", 100);
        if (random.nextInt(100) >= chance) return;

        Player player = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
        World world = pickRandomWorld();
        if (world == null) return;

        if (plugin.getConfig().getBoolean("warning.enabled", true)) {
            int countdown = plugin.getConfig().getInt("warning.countdown", 10);
            sendCountdown(player, countdown);
        } else {
            teleportPlayerSafe(player, world);
        }
    }

    private void sendCountdown(Player player, int countdown) {
        new BukkitRunnable() {
            int timeLeft = countdown;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String message = plugin.getConfig().getString("warning.message", "&cWarning! Dimension shift in %time% seconds!")
                            .replace("%time%", String.valueOf(timeLeft));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    timeLeft--;
                } else {
                    String finalMessage = plugin.getConfig().getString("warning.final-message", "&4Teleporting now!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalMessage));
                    teleportPlayerSafe(player, pickRandomWorld());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void teleportPlayerSafe(Player player, World world) {
        if (world == null) return;

        Location safeLocation = getSafeLocation(world);

        if (safeLocation != null) {
            player.teleport(safeLocation);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to find a safe teleport location!");
        }
    }

    private Location getSafeLocation(World world) {
        int maxTries = 50;
        int range = plugin.getConfig().getInt("teleport-range", 1000);

        for (int i = 0; i < maxTries; i++) {
            int x = random.nextInt(range * 2) - range;
            int z = random.nextInt(range * 2) - range;

            int y = getSafeY(world, x, z);
            if (y != -1) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }
        return world.getSpawnLocation();  // Fallback to world spawn if no safe location found
    }

    private int getSafeY(World world, int x, int z) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return getNetherSafeY(world, x, z);
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            return getEndSafeY(world, x, z);
        } else {
            return getOverworldSafeY(world, x, z);
        }
    }

    private int getNetherSafeY(World world, int x, int z) {
        for (int y = 124; y > 5; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block below = world.getBlockAt(x, y - 1, z);

            if (isSafeBlock(block) && below.getType().isSolid() && above.getType() == Material.AIR) {
                return y;
            }
        }
        return -1;
    }

    private int getEndSafeY(World world, int x, int z) {
        for (int y = world.getHighestBlockYAt(x, z); y > 0; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() == Material.END_STONE) return y + 1;
        }
        return -1;
    }

    private int getOverworldSafeY(World world, int x, int z) {
        for (int y = world.getHighestBlockYAt(x, z); y > 5; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block below = world.getBlockAt(x, y - 1, z);
            Block above = world.getBlockAt(x, y + 1, z);

            if (isSafeBlock(block) && below.getType().isSolid() && above.getType() == Material.AIR) {
                return y;
            }
        }
        return -1;
    }

    private boolean isSafeBlock(Block block) {
        Material type = block.getType();
        return type != Material.LAVA && type != Material.FIRE && type != Material.WATER
                && type != Material.CACTUS && type != Material.MAGMA_BLOCK && type != Material.CAMPFIRE
                && type != Material.SOUL_CAMPFIRE && type != Material.POWDER_SNOW && type != Material.AIR;
    }

    private World pickRandomWorld() {
        List<String> allowedWorlds = plugin.getConfig().getStringList("allowed-worlds");
        if (allowedWorlds.isEmpty()) return null;

        String worldName = allowedWorlds.get(random.nextInt(allowedWorlds.size()));
        return Bukkit.getWorld(worldName);
    }
}
