package com.calico.launcher.emulators

import com.calico.launcher.data.EmulatorIds
import com.calico.launcher.model.Game

class EmulatorRegistry {
    private val launchers = mapOf(
        EmulatorIds.RETRO_ARCH to RetroArchLauncher(),
        EmulatorIds.DOLPHIN to DolphinLauncher(),
        EmulatorIds.CEMU to CemuLauncher(),
        EmulatorIds.MELON_DS to MelonDsLauncher(),
        EmulatorIds.AZAHAR to AzaharLauncher(),
        EmulatorIds.EDEN to EdenLauncher(),
        EmulatorIds.NETHER_SX2 to NetherSx2Launcher(),
        EmulatorIds.APS3E to Aps3eLauncher(),
        EmulatorIds.PPSSPP to PpssppLauncher(),
        EmulatorIds.EMU_CORE_V to EmuCoreVLauncher(),
        EmulatorIds.FLYCAST to FlycastLauncher(),
    )

    fun launcherFor(game: Game): EmulatorLauncher? = launchers[game.platform.defaultEmulatorId]
}
