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
- Hardcoded emulator launcher classes for RetroArch, Dolphin, Cemu, melonDS, Azahar, Eden, NethersX2, aPS3e, PPSSPP, EmuCoreV, and Flycast
- SteamGridDB hero selection helper that chooses the first high-resolution 16:9 image from API results

Open the project in Android Studio and run the `app` module on a phone, tablet, foldable, or dual-screen Android emulator.
