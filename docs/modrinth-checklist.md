# Modrinth Release Checklist

## Project

- Title: `AAAntylogout`
- Slug: `aaantylogout`
- Summary: `Advanced Paper anti-logout plugin with combat timers, logout punishment, command restrictions, regions and battle history.`
- Description: use `ModrinthReadme.md`.
- License: `MIT`
- Project type: plugin
- Platform/loader: Paper
- Suggested categories: `utility`, `server management`, `game mechanics`
- Minecraft versions: `1.21`
- Java requirement: Java 21
- Optional dependency: WorldGuard

## Version Upload

- Version name: `AAAntylogout 1.1.0`
- Version number: `1.1.0`
- Release channel: release
- File: `AAAntilogout/target/AAAntylogout-1.1.0.jar`
- Changelog: use `ModrinthChangelog.md`.

## Before Publishing

- Run `mvn -q -DskipTests package`.
- Test PvP hit tagging with two players.
- Test logout during combat.
- Test `/aaalo status <player>`, `/aaalo history <player>` and `/aaalo reload`.
- Take gallery screenshots listed in `docs/gallery-plan.md`.
