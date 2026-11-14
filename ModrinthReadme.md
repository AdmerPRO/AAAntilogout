# AAAntylogout

AAAntylogout is a lightweight but fully featured anti-logout plugin for Paper PvP servers. It starts a combat timer when players hit each other, warns them with chat, bossbar and actionbar feedback, blocks common escape methods, and punishes combat logging with death and item drops.

## Why use it?

- Clear 20-second combat timer after PvP hits
- Bossbar, actionbar and chat reminders while tagged
- Combat logout punishment with death, item drops and broadcast messages
- Command restrictions with blacklist or whitelist mode
- Blocked items such as ender pearls, chorus fruit, wind charges and rockets
- Teleport, item drop and elytra restrictions during combat
- Optional WorldGuard support for blocked regions
- Built-in local cuboid regions without any dependency
- Battle history for admins with duration, opponents and result
- Configurable death behavior: surviving opponents can stay tagged after a kill

## Commands

- `/aaalo help`
- `/aaalo reload`
- `/aaalo status <player>`
- `/aaalo history <player> [page]`
- `/aaalo stats <player>`
- `/aaalo end <player>`

Aliases: `/antylogout`, `/al`, `/aaalog`, `/alo`

## Permissions

- `aaantylogout.admin` - access to administration commands
- `aaantylogout.bypass` - bypasses combat tagging and restrictions
- `aaantylogout.bypass.commands` - allows blocked commands during combat

## Requirements

Builds are available for Paper 1.21.x through 26.3. Download the JAR matching your server version.

| Paper version | Required Java version |
| --- | --- |
| 1.21.x | Java 21 or newer |
| 26.1.x | Java 25 or newer |
| 26.2 | Java 25 or newer |
| 26.3 | Java 25 or newer |

WorldGuard is optional.

## Configuration

Everything important is configurable in `config.yml`: combat duration, logout punishment, command mode, blocked items, teleport causes, region behavior, messages, bossbar, actionbar and chat reminders.

**WorldGuard** is optional. If it is installed, names from `regions.blocked` are matched against WorldGuard region IDs. Without WorldGuard, define matching cuboids in `regions.local-regions`.
