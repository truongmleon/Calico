package com.calico.launcher.data

import com.calico.launcher.model.Emulator
import com.calico.launcher.model.Game
import com.calico.launcher.model.GameFile
import com.calico.launcher.model.GameFileType
import com.calico.launcher.model.Platform
import com.calico.launcher.model.TaskbarItem

object EmulatorIds {
    const val RETRO_ARCH = 1
    const val DOLPHIN = 2
    const val CEMU = 3
    const val MELON_DS = 4
    const val AZAHAR = 5
    const val EDEN = 6
    const val NETHER_SX2 = 7
    const val APS3E = 8
    const val PPSSPP = 9
    const val EMU_CORE_V = 10
    const val FLYCAST = 11
}

val DEFAULT_EMULATORS = listOf(
    Emulator(EmulatorIds.RETRO_ARCH, "RetroArch", "com.retroarch", "https://www.retroarch.com/"),
    Emulator(EmulatorIds.DOLPHIN, "Dolphin", "org.dolphinemu.dolphinemu", "https://dolphin-emu.org/"),
    Emulator(EmulatorIds.CEMU, "Cemu", "info.cemu.Cemu", "https://cemu.info/"),
    Emulator(EmulatorIds.MELON_DS, "melonDS", "me.magnum.melonds", "https://melonds.kuribo64.net/"),
    Emulator(EmulatorIds.AZAHAR, "Azahar", "io.github.lime3ds.android", "https://azahar-emu.org/"),
    Emulator(EmulatorIds.EDEN, "Eden", "dev.eden.eden_emulator", "https://eden-emu.dev/"),
    Emulator(EmulatorIds.NETHER_SX2, "NethersX2", "xyz.aethersx2.android", "https://nethersx2.com/"),
    Emulator(EmulatorIds.APS3E, "aPS3e", "com.aps3e.android", "https://aps3e.com/"),
    Emulator(EmulatorIds.PPSSPP, "PPSSPP", "org.ppsspp.ppsspp", "https://www.ppsspp.org/"),
    Emulator(EmulatorIds.EMU_CORE_V, "EmuCoreV", "com.emucore.psvita", "https://emucore.org/"),
    Emulator(EmulatorIds.FLYCAST, "Flycast", "com.flycast.emulator", "https://flyinghead.github.io/flycast-builds/"),
)

val DEFAULT_PLATFORMS = listOf(
    Platform(1, "NES", "nes", listOf("nes", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(2, "SNES", "snes", listOf("sfc", "smc", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(3, "Game Boy", "gb", listOf("gb", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(4, "Game Boy Color", "gbc", listOf("gbc", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(5, "Game Boy Advance", "gba", listOf("gba", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(6, "Nintendo 64", "n64", listOf("n64", "z64", "v64", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(7, "PlayStation", "ps1", listOf("chd", "cue", "bin", "pbp"), EmulatorIds.RETRO_ARCH),
    Platform(8, "Genesis", "genesis", listOf("md", "gen", "bin", "zip"), EmulatorIds.RETRO_ARCH),
    Platform(9, "Sega CD", "segacd", listOf("chd", "cue", "iso"), EmulatorIds.RETRO_ARCH),
    Platform(10, "Saturn", "saturn", listOf("chd", "cue", "iso"), EmulatorIds.RETRO_ARCH),
    Platform(11, "GameCube", "gamecube", listOf("iso", "rvz", "gcm"), EmulatorIds.DOLPHIN),
    Platform(12, "Wii", "wii", listOf("iso", "rvz", "wbfs"), EmulatorIds.DOLPHIN),
    Platform(13, "Wii U", "wiiu", listOf("wua", "wux", "wud"), EmulatorIds.CEMU),
    Platform(14, "Nintendo DS", "ds", listOf("nds", "zip"), EmulatorIds.MELON_DS),
    Platform(15, "Nintendo 3DS", "3ds", listOf("3ds", "cci", "cia"), EmulatorIds.AZAHAR),
    Platform(16, "Nintendo Switch", "switch", listOf("nsp", "xci"), EmulatorIds.EDEN),
    Platform(17, "PlayStation 2", "ps2", listOf("iso", "chd", "bin"), EmulatorIds.NETHER_SX2),
    Platform(18, "PlayStation 3", "ps3", listOf("pkg", "iso"), EmulatorIds.APS3E),
    Platform(19, "PSP", "psp", listOf("iso", "cso"), EmulatorIds.PPSSPP),
    Platform(20, "PS Vita", "psvita", listOf("vpk", "pkg"), EmulatorIds.EMU_CORE_V),
    Platform(21, "Dreamcast", "dreamcast", listOf("chd", "gdi", "cdi"), EmulatorIds.FLYCAST),
)

val SAMPLE_GAMES = listOf(
    sampleGame(1, DEFAULT_PLATFORMS[1], "Chrono Trigger", "1995", listOf("Square"), listOf("RPG"), 188_400),
    sampleGame(2, DEFAULT_PLATFORMS[1], "Super Metroid", "1994", listOf("Nintendo R&D1"), listOf("Action"), 54_300),
    sampleGame(3, DEFAULT_PLATFORMS[6], "Metal Gear Solid", "1998", listOf("Konami"), listOf("Stealth"), 91_800),
    sampleGame(4, DEFAULT_PLATFORMS[10], "F-Zero GX", "2003", listOf("Amusement Vision"), listOf("Racing"), 31_200),
    sampleGame(5, DEFAULT_PLATFORMS[13], "Mario Kart DS", "2005", listOf("Nintendo EAD"), listOf("Racing"), 72_600),
    sampleGame(6, DEFAULT_PLATFORMS[18], "Lumines", "2004", listOf("Q Entertainment"), listOf("Puzzle"), 24_100),
)

val SAMPLE_TASKBAR_ITEMS = listOf(
    TaskbarItem(1, "YouTube", "com.google.android.youtube", null, "app", 0),
    TaskbarItem(2, "Spotify", "com.spotify.music", null, "app", 1),
    TaskbarItem(3, "Achievements", null, null, "system_action", 2),
    TaskbarItem(4, "Browser", "com.android.chrome", null, "app", 3),
)

private fun sampleGame(
    id: Int,
    platform: Platform,
    name: String,
    releaseDate: String,
    developers: List<String>,
    genres: List<String>,
    durationSeconds: Int,
) = Game(
    id = id,
    platform = platform,
    name = name,
    sortTitle = name.lowercase(),
    description = "$name is ready to launch once an Emulation root with roms, media, and metadata.db is selected.",
    releaseDate = releaseDate,
    developers = developers,
    genres = genres,
    durationSeconds = durationSeconds,
    lastPlayedAt = null,
    isFavorite = id % 2 == 0,
    primaryFile = GameFile(
        id = id,
        gameId = id,
        platformId = platform.id,
        fileType = GameFileType.Base,
        path = "/Emulation/roms/${platform.romFolderName}/$name.${platform.supportedExtensions.first()}",
        name = "$name.${platform.supportedExtensions.first()}",
        extension = platform.supportedExtensions.first(),
    ),
)
