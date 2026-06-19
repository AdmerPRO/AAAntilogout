# AAAntylogout

AAAntylogout is an advanced Paper anti-logout plugin for PvP servers. It tags players in combat, punishes combat logging, stores battle history, and blocks common escape methods such as commands, pearls, chorus fruit, teleportation and protected regions.

## Highlights

- Combat timer with bossbar, actionbar and chat reminder feedback
- Combat logout punishment with item drops
- Admin history: duration, opponents and result for each fight
- `/aaalo` command with aliases `/antylogout`, `/al`, `/aaalog`, `/alo`
- Command restrictions in blacklist or whitelist mode
- Blocked items and teleport causes
- Optional WorldGuard region support
- Built-in local cuboid regions without dependencies
- Fully configurable messages and behavior

## Requirements

- Paper 1.21+
- Java 21
- WorldGuard is optional

## Commands

- `/aaalo help`
- `/aaalo reload`
- `/aaalo status <player>`
- `/aaalo history <player> [page]`
- `/aaalo stats <player>`
- `/aaalo end <player>`

## Permissions

- `aaantylogout.admin`
- `aaantylogout.bypass`
- `aaantylogout.bypass.commands`

## Notes

The plugin works without WorldGuard. If WorldGuard is installed, names from `regions.blocked` are matched against WorldGuard region IDs. Without WorldGuard, define the same names in `regions.local-regions`.
