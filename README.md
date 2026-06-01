# Calico

- A full featured emulation frontend inspired by the 3DS UI built for single and dual screen Android devices
- Mainly focused for dual screen
- Games can be sorted and collected
- Android apps won’t be in the main emulation section, but rather in a customizable bottom navigation bar similar to the Windows taskbar
- Backgrounds of top and bottom screen are initially white, but can set a background to them
- Background music can be played from downloaded music (mp3, wav, etc.)
- Users can created albums inside
- Sound effects for opening specific pages and menus (Select, Details, Menu buttons) can be set
- Background wallpapers can be changed on either screen
- Fluid transitions and responsive interactions
- Emulators
  - NES/SNES/GB/GBC/GBA/N64/PS1/Genesis/Sega CD/Saturn: RetroArch
  - Gamecube/Wii: Dolphin
  - Wii U: Cemu
  - DS: melonDS
  - 3DS: Azahar
  - Switch: Eden
  - PS2: NethersX2
  - PS3: aPS3e
  - PSP: PPSSPP
  - PS Vita: EmuCoreV
  - Dreamcast: Flycast

## Current Implementation

This repository now contains a Kotlin Android app scaffold for the launcher:

- Jetpack Compose dual-screen UI with a top hero/details area and a bottom interactive game grid
- Bottom taskbar for Android apps, bookmarks, and system actions
- Sort drawer for console, name, last played, total hours, and favorites
- Menu drawer for music controls, wallpapers, taskbar, folder selection, metadata, and API login entry points
- SQLite `metadata.db` schema matching the product document tables
- Scan-root validation for `/roms`, `/media`, and `metadata.db`
- ROM library scanning that groups base games, updates, DLC, patches, saves, manuals, and multi-disc files under one game entry
- Background music scanned from `Emulation/music/` when the emulation root is loaded
- Hardcoded emulator launcher classes for RetroArch, Dolphin, Cemu, melonDS, Azahar, Eden, NethersX2, aPS3e, PPSSPP, EmuCoreV, and Flycast
- SteamGridDB hero selection helper that chooses the first high-resolution 16:9 image from API results

Open the project in Android Studio and run the `app` module on a phone, tablet, foldable, or dual-screen Android emulator.

## Emulation Folder Structure

Calico expects the selected Emulation root to contain `roms`, `media`, and `metadata.db`. Background music is loaded from `music/` under the same root (create the folder when you add tracks):

```text
/Emulation/
  roms/
  media/
  music/
  metadata.db
```

Supported audio formats in `music/`: `mp3`, `wav`, `ogg`, `flac`, `m4a`, `aac`. Subfolders are supported (the parent folder name is used as the artist label when scanning from disk).

Each platform uses its configured ROM folder name, such as `switch`, `wiiu`, `ps1`, or `gamecube`. A platform folder can contain loose ROM files for simple libraries:

```text
/Emulation/
  roms/
    snes/
      Chrono Trigger.sfc
      Super Metroid.sfc
```

For games with updates, DLC, multi-disc media, or extracted content, use **one folder per game** with `base/`, `updates/`, and `dlc/` inside:

```text
/Emulation/
  roms/
    switch/
      Mario Kart 8 Deluxe/
        base/
          Mario Kart 8 Deluxe.xci
        updates/
          v3.0.3.nsp
        dlc/
          Booster Course Pass.nsp

    ps1/
      Metal Gear Solid/
        discs/
          Disc 1.chd
          Disc 2.chd
```

Extracted Wii U/Cemu-style folders are supported when a scanned folder contains `code`, `content`, and `meta`:

```text
/Emulation/
  roms/
    wiiu/
      Mario Kart 8/
        base/
          code/
          content/
          meta/
        updates/
          v81/
            code/
            content/
            meta/
        dlc/
          Pack 1/
            code/
            content/
            meta/
```

Recognized child folders are `base`, `game`, `updates`, `update`, `dlc`, `addons`, `add-ons`, `discs`, `disc`, `patches`, `patch`, `saves`, `save`, `manuals`, and `manual`. `base` and `disc` files are launchable; updates and DLC are tracked as attached game files for install/import flows rather than shown as separate games in the main grid.

### Nintendo Switch and Eden (external content)

Calico library layout (bases under `switch/`):

```text
/Emulation/
  roms/
    switch/
      Mario Kart 8 Deluxe/
        base/
          Mario Kart 8 Deluxe.xci
```

Eden external content layout (updates and DLC — Eden-only, not a different Calico layout rule):

```text
/Emulation/
  roms/
    switch_extras/
      Mario Kart 8 Deluxe/
        updates/
          v3.0.3.nsp
        dlc/
          Booster Course Pass.nsp
```

| Eden setting | Path |
| --- | --- |
| Games folder | `Emulation/roms/switch/` |
| External content folder | `Emulation/roms/switch_extras/` |

Game folder names must match between `switch/` and `switch_extras/`. `switch_extras/` exists because Eden on Android cannot use the same root path for both games and external content.

### Platform folder reference

| Platform folder | Emulator | Structure | Updates / DLC / extras |
| --- | --- | --- | --- |
| `snes`, `nes`, `gb`, `gbc`, `gba`, `n64`, `genesis` | RetroArch | Loose file or `<Game>/base/` | Not needed |
| `ps1`, `saturn`, `segacd` | RetroArch | Loose file or `<Game>/discs/` | Multi-disc only |
| `gamecube`, `wii` | Dolphin | Loose `.iso` / `.rvz` | Not needed |
| `wiiu` | Cemu | `<Game>/base/` (+ optional `updates/`, `dlc/` with `code/content/meta`) | Cemu-side install for encrypted content |
| `ds` | melonDS | Loose `.nds` | Not needed |
| `3ds` | Azahar | Loose `.3ds` / `.cci` | `.cia` may need install in Azahar |
| `switch` | Eden | `<Game>/base/` + Eden `switch_extras/` for updates/DLC | Eden external content or NAND install |
| `ps2` | NethersX2 | Loose `.iso` / `.chd` | Not needed |
| `ps3` | aPS3e | Loose `.iso` or `.pkg` | `.pkg` may need install in aPS3e |
| `psp` | PPSSPP | Loose `.iso` / `.cso` | Not needed |
| `psvita` | EmuCoreV | `.vpk` / `.pkg` | May need install in emulator |
| `dreamcast` | Flycast | `.chd` or `.gdi` + `.bin` tracks in the same folder | Not needed |

### Emulator setup (one-time, not folder layout)

| Emulator | Extra setup beyond folders |
| --- | --- |
| Eden | Prod keys, firmware, games folder + external content folder |
| RetroArch | Cores per system |
| Cemu | Keys for encrypted Wii U content |
| Azahar | System files; CIA install if using `.cia` |
| aPS3e / EmuCoreV | Install step for some `.pkg` files |
| Dolphin, PPSSPP, melonDS, etc. | Usually launch-ready after ROM scan |
