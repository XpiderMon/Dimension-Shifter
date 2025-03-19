package me.mejbha.dimensionShifter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;

import java.util.List;

public class DimensionShifter extends JavaPlugin {

    private WarpTask warpTask;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Create config.yml if missing
        reloadConfig(); // Reload config values

        // Register command executor
        getCommand("ds").setExecutor(this);

        // Start warp task
        startWarpTask();

        // Register Respawn Listener
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        new Metrics(this, 25148);

        getLogger().info("=================================");
        getLogger().info("DimensionShifter enabled!");
        getLogger().info("          Author  : Mejbha");
        getLogger().info("          Version : 2.0");
        getLogger().info("======================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("DimensionShifter disabled!");
    }

    public void startWarpTask() {
        if (warpTask != null) {
            warpTask.cancel(); // Stop existing task before starting a new one
        }

        long interval = getConfig().getInt("warp-interval", 300) * 20L; // Convert seconds to ticks
        warpTask = new WarpTask(this);
        warpTask.runTaskTimer(this, interval, interval);
    }

    // Reload command: /ds reload
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            startWarpTask(); // Restart the warp task with new settings
            sender.sendMessage("§aDimensionShifter config reloaded!");
            return true;
        }
        return false;
    }

}
