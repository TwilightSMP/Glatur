# Glatur

Server-side Fabric mod that adds an `/invins` list. Players in that list cannot die and are clamped to 1 HP.

## Current Decisions

- Mod id: `glatur`
- Name: `Glatur`
- Baseline Minecraft target in this scaffold: `1.21.11`
- Persistence method: JSON file at `<world>/data/glatur/invins.json`
- Command permission level: `4` (highest operator level)

## Command Usage

- `/invins add <player>`
- `/invins remove <player>`
- `/invins list`

## Behavior Notes

- Uses UUIDs only for storage and checks.
- Death for tracked players is cancelled server-side.
- Tracked players are clamped to exactly `1.0F` HP on server ticks.
- Players can be removed from the JSON-backed list with `/invins remove <player>`.
- `/kill` and void-like lethal flows are handled via death cancellation; tracked players remain alive at 1 HP.

## Build

1. Install Gradle locally or add Gradle wrapper files for this project.
2. Run `gradle build` from this project directory.
3. Place produced JAR from `build/libs` into your Fabric server `mods` directory.

## Verification Checklist

1. Start server and verify Glatur logs initialization.
2. Run `/invins add <player>` and confirm feedback.
3. Apply lethal damage and verify health clamps at 1 HP without death.
4. Run `/invins remove <player>` and verify the player can die again.
5. Restart server and verify `/invins list` still contains persisted entries.
