# AAAntylogout

AAAntylogout is an advanced anti-logout plugin for Paper servers. It punishes combat logging, stores detailed battle history, and blocks common escape methods such as commands, items, teleportation, elytra and protected regions.

## Features

- PvP tagging after melee hits and projectile hits.
- Combat logout punishment with death, item drops and an optional server broadcast.
- Battle history in `battle-history.yml`: duration, opponents and result (`timeout`, `death`, `logout`, `admin`, `server_stop`).
- Administration command `/aaalo` with aliases `/antylogout`, `/al`, `/aaalog`, `/alo`.
- Command restriction mode: blacklist or whitelist.
- Item, teleport, item drop and elytra restrictions during combat.
- Optional WorldGuard region support.
- Built-in local cuboid regions when WorldGuard is not installed.
- Bossbar with remaining combat time.
- Ready-to-publish Modrinth metadata and documentation.

## Requirements

- Paper 1.21+
- Java 21
- WorldGuard optional

## Installation

1. Build the plugin with `mvn clean package` in the `AAAntilogout` folder.
2. Put `AAAntylogout-1.0-SNAPSHOT.jar` from `target` into your server `plugins` folder.
3. Start the server, edit `plugins/AAAntylogout/config.yml`, then run `/aaalo reload`.

## Commands

| Command | Description |
| --- | --- |
| `/aaalo help` | Shows administration commands |
| `/aaalo reload` | Reloads the configuration |
| `/aaalo status <player>` | Shows a player's active combat |
| `/aaalo history <player> [page]` | Shows battle history |
| `/aaalo stats <player>` | Shows battle statistics |
| `/aaalo end <player>` | Ends a player's combat |

## Permissions

| Permission | Description |
| --- | --- |
| `aaantylogout.admin` | Access to administration commands |
| `aaantylogout.bypass` | Prevents tagging and bypasses combat restrictions |
| `aaantylogout.bypass.commands` | Allows blocked commands during combat |

## Regions

Set blocked region names in `regions.blocked`. If WorldGuard is installed, AAAntylogout matches those names against WorldGuard region IDs. Without WorldGuard, define matching cuboids in `regions.local-regions`.

## Build

```bash
cd AAAntilogout
mvn clean package
```

## License

MIT. See [LICENSE.md](LICENSE.md).
