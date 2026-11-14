# Configuration

The default configuration is generated as `plugins/AAAntylogout/config.yml`.

## Combat

`combat.duration-seconds` controls how long a player stays tagged. `combat.refresh-timer-on-hit` decides whether every hit resets the timer.

`combat.keep-timer-after-opponent-death` controls what happens after a combat death. When it is `true`, only the dead player leaves combat and surviving opponents stay tagged until their timer ends. When it is `false`, a death ends combat for everyone involved.

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

## Restart protection

When Paper reports that the server is stopping, active fights end without logout
punishments or punishment broadcasts. This also covers shutdowns initiated by
plugins or the hosting panel when they use normal server shutdown.

Active sessions are checkpointed in the `active-sessions` section of
`battle-history.yml` when a new fight starts and once per second during combat.
After an unexpected shutdown, the next enable consumes those snapshots as
`server_stop` history entries. Timers and penalties are never restored. The
recorded end time is the last checkpoint, not the time the server starts again.
Up to one second of recent combat updates can be missing after a crash.

`history.record-server-stop: false` skips stop/recovery history entries while
keeping protection enabled. Snapshots are still cleared so they cannot be
recovered on a later restart.

A panel that kicks players before initiating server shutdown must be configured
to stop the server normally; a kick while the server is still running is treated
as an ordinary logout.
