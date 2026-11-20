# gamexy

A third-person, Diablo-style Action RPG built with **Java**, **libGDX**, and **gdx-gltf** using standalone `.glb` 3D model assets.

## Controls

| Input | Action |
|---|---|
| **W / S** | Move Barbarian forward / backward |
| **A / D** | Turn Barbarian left / right (camera smoothly follows behind) |
| **Left-Click** (click or hold) | Use the currently selected ability aimed at the mouse cursor |
| **1 – 9, 0** or **Click Toolbar Slot** | Select active ability on the bottom toolbar |
| **Right-Click Hold & Move** (or **Alt + Drag**) | Free-orbit and pitch the camera (overrides auto-follow while held) |
| **Mouse Scroll** | Zoom camera in / out |
| **R** | Respawn if defeated |
| **Esc** | Pause / Resume game (Pause Menu) |

## Barbarian Abilities

- **Level 1 — Cleave** (Slot 1): Wide melee axe arc that generates `+18 Fury`.
- **Level 1 — Ground Slam** (Slot 2): AoE shockwave that stuns and knocks back enemies (`30 Fury`).
- **Level 2 — Whirlwind** (Slot 3): 360° spinning axe strike (`20 Fury`).
- **Level 3 — War Cry** (Slot 4): Restores `35% HP`, grants `+35 Fury`, knocks back foes, and boosts damage `+35%` for 8 seconds.

## Project Modules

- `tools/` — Procedural glTF 2.0 `.glb` generator (`./gradlew :tools:generateAssets`). Outputs standalone `.glb` files into `assets/models/` that can be inspected or swapped out in Blender.
- `core/` — Game engine, Barbarian & enemy logic, combat, camera, dungeon world, and Diablo-style HUD.
- `desktop/` — Desktop LWJGL3 launcher.

## Running the Game

```powershell
.\gradlew.bat :desktop:run
```
