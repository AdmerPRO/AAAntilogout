# Configuration

The default configuration is generated as `plugins/AAAntylogout/config.yml`.

## Combat

`combat.duration-seconds` controls how long a player stays tagged. `combat.refresh-timer-on-hit` decides whether every hit resets the timer.

## Commands

`restrictions.commands.mode` accepts `BLACKLIST` or `WHITELIST`.

- `BLACKLIST`: commands on the list are blocked.
- `WHITELIST`: only commands on the list are allowed.

Command names can be written with or without `/`.

## Items

Add Bukkit material names to `restrictions.items.blocked`, for example `ENDER_PEARL`, `CHORUS_FRUIT` or `FIREWORK_ROCKET`.

## Regions

`regions.blocked` contains names that are forbidden during combat. With WorldGuard, these names match WorldGuard region IDs. Without WorldGuard, define cuboids in `regions.local-regions`.

## Visuals

`visuals.bossbar.enabled` controls the bossbar. `visuals.actionbar.enabled` controls the hotbar/actionbar text. `visuals.chat-reminder.enabled` and `visuals.chat-reminder.interval-seconds` control repeated chat messages with remaining combat time.
