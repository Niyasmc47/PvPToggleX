# PvPToggleX

**PvPToggleX** is a versatile and lightweight Minecraft plugin designed to give server administrators complete control over PvP (Player vs. Player) combat. Whether you need to toggle PvP globally, per dimension, or set up automatic intervals, PvPToggleX handles it all. It also includes smart features like lava placement restrictions to prevent griefing when PvP is disabled.

![Java CI](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0-blue)
![API](https://img.shields.io/badge/API-1.20-yellow)

## Features

-   **Global PvP Control**: Toggle PvP on or off for the entire server with a single command.
-   **Dimension-Specific Control**: Manage PvP settings independently for the Overworld, Nether, and End.
-   **Timed Delays**: Schedule PvP changes to happen after a set delay (e.g., enable PvP in 10 seconds).
-   **Automatic Toggling**: Set up recurring intervals where PvP automatically switches on and off (useful for event servers).
-   **Anti-Griefing**: Automatically blocks players from placing lava buckets near others when PvP is disabled.
-   **Fully Configurable**: Customize all messages, default intervals, and safety checks via `config.yml`.

## Installation

1.  Download the `PVPtoggleX-1.0.jar` file.
2.  Place the JAR file into your server's `plugins` folder.
3.  Restart your server.
4.  Edit `plugins/PvPToggleX/config.yml` to customize settings if desired.
5.  Run `/pvptogglex reload` to apply changes.

## Commands

### Global PvP
*   `/pvp <on|off> [time]` - Turn PvP on or off for all worlds. Optional `[time]` argument delays the action by X seconds.
*   `/pvptoggle <time> | /pvptoggle off` - Start an automatic toggle loop every X seconds, or stop it.

### Dimension Specific
*   `/pvpoverworld <on|off> [time]` - Toggle PvP for the Overworld.
*   `/pvpnether <on|off> [time]` - Toggle PvP for the Nether.
*   `/pvpend <on|off> [time]` - Toggle PvP for the End.

### Dimension Auto-Toggle
*   `/pvptoggleoverworld <time> | off` - Auto-toggle loop for Overworld.
*   `/pvptogglenether <time> | off` - Auto-toggle loop for Nether.
*   `/pvptoggleend <time> | off` - Auto-toggle loop for End.

### Admin
*   `/pvptogglex reload` - Reloads the plugin configuration.

## Permissions

*   `pvptoggle.use` - Allows access to all PvPToggleX commands (Default: OP).

## Configuration

The `config.yml` allows you to tweak various aspects of the plugin:

```yaml
# The interval in seconds for automatic PvP toggling.
toggleInterval: 0

# Disable lava placement when PvP is disabled to prevent griefing
disable-lava: true

# Radius in blocks to check for nearby players when placing lava
lava-radius: 2

# Messages
messages:
  pvp-immediate: "PvP is now §f{state}§r!"
  # ... and many more customizable messages
```

## Building from Source

To build this project locally, you will need JDK 17 or higher and Maven.

1.  Clone the repository:
    ```bash
    git clone https://github.com/yourusername/PVPtoggleX.git
    cd PVPtoggleX
    ```
2.  Build with Maven:
    ```bash
    mvn clean package
    ```
3.  The compiled JAR will be in the `target/` directory.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
