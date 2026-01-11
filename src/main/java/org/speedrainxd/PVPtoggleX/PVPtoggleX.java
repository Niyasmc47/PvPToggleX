package org.speedrainxd.PVPtoggleX;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class PVPtoggleX extends JavaPlugin implements Listener {
    private boolean pvpEnabled = true;
    private BukkitTask toggleTask;
    private BukkitTask toggleTaskNether;
    private BukkitTask toggleTaskEnd;
    private BukkitTask toggleTaskOverworld;

    // Track PvP state per dimension
    private final Map<World.Environment, Boolean> dimensionPvPState = new HashMap<>();

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        
        // Initialize dimension states
        dimensionPvPState.put(World.Environment.NORMAL, true);
        dimensionPvPState.put(World.Environment.NETHER, true);
        dimensionPvPState.put(World.Environment.THE_END, true);
        
        this.getLogger().info("PvPToggle has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("PvPToggle has been disabled.");
        if (this.toggleTask != null) {
            this.toggleTask.cancel();
        }
        if (this.toggleTaskNether != null) {
            this.toggleTaskNether.cancel();
        }
        if (this.toggleTaskEnd != null) {
            this.toggleTaskEnd.cancel();
        }
        if (this.toggleTaskOverworld != null) {
            this.toggleTaskOverworld.cancel();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("pvp")) {
            return handleGlobalPvpCommand(sender, args);
        } else if (cmdName.equals("pvptoggle")) {
            return handleGlobalToggleCommand(sender, args);
        } else if (cmdName.equals("pvpnether")) {
            return handleDimensionPvpCommand(sender, World.Environment.NETHER, "Nether", args);
        } else if (cmdName.equals("pvpend")) {
            return handleDimensionPvpCommand(sender, World.Environment.THE_END, "End", args);
        } else if (cmdName.equals("pvpoverworld")) {
            return handleDimensionPvpCommand(sender, World.Environment.NORMAL, "Overworld", args);
        } else if (cmdName.equals("pvptogglenether")) {
            return handleDimensionToggleCommand(sender, World.Environment.NETHER, "Nether", args);
        } else if (cmdName.equals("pvptoggleend")) {
            return handleDimensionToggleCommand(sender, World.Environment.THE_END, "End", args);
        } else if (cmdName.equals("pvptoggleoverworld")) {
            return handleDimensionToggleCommand(sender, World.Environment.NORMAL, "Overworld", args);
        } else if (cmdName.equals("pvptogglex")) {
            return handlePvPToggleXCommand(sender, args);
        }

        return false;
    }

    private boolean handleGlobalPvpCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getMessage("pvp-usage"));
            return false;
        }

        String action = args[0].toLowerCase();
        if (!action.equals("on") && !action.equals("off")) {
            sender.sendMessage(getMessage("pvp-invalid-arg"));
            return false;
        }

        boolean enable = action.equals("on");
        this.handlePvPCommand(sender, enable, args);
        return true;
    }

    private boolean handleGlobalToggleCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getMessage("pvptoggle-usage"));
            return false;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (this.toggleTask != null) {
                this.toggleTask.cancel();
                this.toggleTask = null;
                sender.sendMessage(getMessage("pvptoggle-stopped"));
            } else {
                sender.sendMessage(getMessage("pvptoggle-not-active"));
            }
        } else {
            try {
                int interval = Integer.parseInt(args[0]);
                if (interval > 0) {
                    this.getConfig().set("toggleInterval", interval);
                    this.saveConfig();
                    if (this.toggleTask != null) {
                        this.toggleTask.cancel();
                    }

                    this.toggleTask = (new BukkitRunnable() {
                        public void run() {
                            PVPtoggleX.this.togglePvP(!PVPtoggleX.this.pvpEnabled);
                            PVPtoggleX.this.broadcastPvPStatus();
                        }
                    }).runTaskTimer(this, 0L, (long) interval * 20L);
                    sender.sendMessage(replaceMessage("pvptoggle-started", "{interval}", String.valueOf(interval)));
                } else {
                    sender.sendMessage(getMessage("pvptoggle-invalid-time"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("pvptoggle-invalid-format"));
            }
        }

        return true;
    }

    private boolean handleDimensionPvpCommand(CommandSender sender, World.Environment dimension, String dimensionName, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(replaceMessage("pvp-dimension-usage", "{dimension}", dimensionName.toLowerCase()));
            return false;
        }

        String action = args[0].toLowerCase();
        if (!action.equals("on") && !action.equals("off")) {
            sender.sendMessage(replaceMessage("pvp-dimension-invalid-arg", "{dimension}", dimensionName.toLowerCase()));
            return false;
        }

        boolean enable = action.equals("on");
        this.handleDimensionPvPCommand(sender, dimension, dimensionName, enable, args);
        return true;
    }

    private boolean handleDimensionToggleCommand(CommandSender sender, World.Environment dimension, String dimensionName, String[] args) {
        if (args.length != 1) {
            String usage = replaceMessage("pvptoggle-dimension-usage", "{dimension}", dimensionName.toLowerCase());
            sender.sendMessage(usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("off")) {
            BukkitTask taskToCancel = getToggleTask(dimension);
            if (taskToCancel != null) {
                taskToCancel.cancel();
                setToggleTask(dimension, null);
                sender.sendMessage(replaceMessage("pvptoggle-dimension-stopped", "{dimension}", dimensionName));
            } else {
                sender.sendMessage(replaceMessage("pvptoggle-dimension-not-active", "{dimension}", dimensionName));
            }
        } else {
            try {
                int interval = Integer.parseInt(args[0]);
                if (interval > 0) {
                    BukkitTask existingTask = getToggleTask(dimension);
                    if (existingTask != null) {
                        existingTask.cancel();
                    }

                    BukkitTask newTask = (new BukkitRunnable() {
                        public void run() {
                            boolean currentState = dimensionPvPState.getOrDefault(dimension, true);
                            toggleDimensionPvP(dimension, !currentState);
                            broadcastDimensionPvPStatus(dimension, dimensionName);
                        }
                    }).runTaskTimer(this, 0L, (long) interval * 20L);

                    setToggleTask(dimension, newTask);
                    String msg = replaceMessage("pvptoggle-dimension-started", "{dimension}", dimensionName);
                    msg = msg.replace("{interval}", String.valueOf(interval));
                    sender.sendMessage(msg);
                } else {
                    sender.sendMessage(getMessage("pvptoggle-invalid-time"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("pvptoggle-invalid-format"));
            }
        }

        return true;
    }

    private void handlePvPCommand(CommandSender sender, final boolean enable, String[] args) {
        if (args.length == 2) {
            try {
                int time = Integer.parseInt(args[1]);
                (new BukkitRunnable() {
                    public void run() {
                        PVPtoggleX.this.togglePvP(enable);
                        PVPtoggleX.this.broadcastPvPStatus();
                    }
                }).runTaskLater(this, (long) time * 20L);
                String state = enable ? getMessage("status-enabled") : getMessage("status-disabled");
                String msg = replaceMessage("pvp-delayed", "{state}", state);
                msg = msg.replace("{time}", String.valueOf(time));
                sender.sendMessage(msg);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("pvptoggle-invalid-format"));
            }
        } else {
            this.togglePvP(enable);
            this.broadcastPvPStatus();
        }
    }

    private void handleDimensionPvPCommand(CommandSender sender, World.Environment dimension, String dimensionName, final boolean enable, String[] args) {
        if (args.length == 2) {
            try {
                int time = Integer.parseInt(args[1]);
                (new BukkitRunnable() {
                    public void run() {
                        toggleDimensionPvP(dimension, enable);
                        broadcastDimensionPvPStatus(dimension, dimensionName);
                    }
                }).runTaskLater(this, (long) time * 20L);
                String state = enable ? getMessage("status-enabled") : getMessage("status-disabled");
                String msg = replaceMessage("pvp-dimension-delayed", "{dimension}", dimensionName);
                msg = msg.replace("{state}", state);
                msg = msg.replace("{time}", String.valueOf(time));
                sender.sendMessage(msg);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("pvptoggle-invalid-format"));
            }
        } else {
            toggleDimensionPvP(dimension, enable);
            broadcastDimensionPvPStatus(dimension, dimensionName);
        }
    }

    private void togglePvP(boolean enable) {
        this.pvpEnabled = enable;
        // Update all dimensions
        dimensionPvPState.put(World.Environment.NORMAL, enable);
        dimensionPvPState.put(World.Environment.NETHER, enable);
        dimensionPvPState.put(World.Environment.THE_END, enable);
        
        Bukkit.getWorlds().forEach((world) -> {
            world.setPVP(enable);
        });
    }

    private void toggleDimensionPvP(World.Environment dimension, boolean enable) {
        dimensionPvPState.put(dimension, enable);
        String dimensionName = getDimensionName(dimension);
        
        Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == dimension)
                .forEach(world -> world.setPVP(enable));
    }

    private void broadcastPvPStatus() {
        String status = this.pvpEnabled ? getMessage("status-enabled") : getMessage("status-disabled");
        String msg = replaceMessage("pvp-immediate", "{state}", status);
        Bukkit.broadcastMessage(msg);
    }

    private void broadcastDimensionPvPStatus(World.Environment dimension, String dimensionName) {
        boolean enabled = dimensionPvPState.getOrDefault(dimension, true);
        String status = enabled ? getMessage("status-enabled") : getMessage("status-disabled");
        String msg = replaceMessage("pvp-dimension-immediate", "{dimension}", dimensionName);
        msg = msg.replace("{state}", status);
        Bukkit.broadcastMessage(msg);
    }

    private String getDimensionName(World.Environment environment) {
        return switch (environment) {
            case NETHER -> "Nether";
            case THE_END -> "End";
            default -> "Overworld";
        };
    }

    private BukkitTask getToggleTask(World.Environment dimension) {
        return switch (dimension) {
            case NETHER -> toggleTaskNether;
            case THE_END -> toggleTaskEnd;
            default -> toggleTaskOverworld;
        };
    }

    private void setToggleTask(World.Environment dimension, BukkitTask task) {
        switch (dimension) {
            case NETHER -> toggleTaskNether = task;
            case THE_END -> toggleTaskEnd = task;
            default -> toggleTaskOverworld = task;
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        World world = event.getBlock().getWorld();
        boolean isPvPEnabled = dimensionPvPState.getOrDefault(world.getEnvironment(), true);
        
        // Check if lava restriction is enabled in config
        boolean disableLava = this.getConfig().getBoolean("disable-lava", true);
        
        if (!isPvPEnabled && event.getBucket() == Material.LAVA_BUCKET && disableLava) {
            // Check if there are other players within the specified radius of the block being filled
            int radius = this.getConfig().getInt("lava-radius", 2);
            if (isPlayerNearbyBlock(event.getBlock(), event.getPlayer(), radius)) {
                event.setCancelled(true);
                String msg = replaceMessage("lava-blocked", "{radius}", String.valueOf(radius));
                event.getPlayer().sendMessage(msg);
            }
        }
    }

    private boolean isPlayerNearbyBlock(org.bukkit.block.Block block, org.bukkit.entity.Player player, int radius) {
        return block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), radius, radius, radius).stream()
                .anyMatch(entity -> entity instanceof org.bukkit.entity.Player && !entity.equals(player));
    }

    private boolean handlePvPToggleXCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getMessage("pvptogglex-usage"));
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig();
            sender.sendMessage(getMessage("pvptogglex-reloaded"));
            this.getLogger().info("PvPToggleX config reloaded by " + sender.getName());
            return true;
        }

        sender.sendMessage(getMessage("pvptogglex-usage"));
        return false;
    }

    private String getMessage(String key) {
        return this.getConfig().getString("messages." + key, key);
    }

    private String replaceMessage(String key, String placeholder, String replacement) {
        String msg = getMessage(key);
        return msg.replace(placeholder, replacement);
    }
}
