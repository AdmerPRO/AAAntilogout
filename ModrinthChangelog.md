# 1.1.0 — Restart and crash protection

## What's new

- **Safe server restarts:** active fights end without combat logout penalties or punishment broadcasts when the server is stopping.
- **Crash recovery:** active combat sessions are saved when a new fight starts and checkpointed every second. After an unexpected shutdown, saved sessions are closed without penalties and recorded as `server_stop` in battle history.
- **Safer history saves:** battle history is written through a temporary file to reduce the risk of corruption if a write is interrupted.

## Notes

- Combat timers and penalties are never restored after a restart or crash.
- `history.record-server-stop: false` disables stop and recovery history entries while keeping protection enabled.
- A crash may lose up to one second of recent combat updates. This feature does not protect against Minecraft player-data rollbacks.
- Restart protection requires a normal server shutdown. Players kicked by a hosting panel before shutdown begins are still treated as ordinary logouts.
- Regular combat logout penalties remain unchanged.
