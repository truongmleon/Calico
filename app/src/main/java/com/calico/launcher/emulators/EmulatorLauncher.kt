package com.calico.launcher.emulators

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.calico.launcher.model.Game

interface EmulatorLauncher {
    val packageName: String

    fun createLaunchIntent(context: Context, game: Game): Intent

    fun launch(context: Context, game: Game) {
        val intent = createLaunchIntent(context, game)
        Log.d("LauncherIntent", "Target: ${intent.component?.packageName ?: intent.`package`}")
        Log.d("LauncherIntent", "Payload: ${intent.getStringExtra(EXTRA_ROM) ?: intent.dataString}")
        try {
            context.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            Log.w("LauncherIntent", "No installed emulator can handle ${game.name}", error)
            Toast.makeText(context, "Install ${game.platform.name}'s emulator to launch this game.", Toast.LENGTH_LONG)
                .show()
        } catch (error: SecurityException) {
            Log.w("LauncherIntent", "Emulator rejected the ROM URI for ${game.name}", error)
            Toast.makeText(context, "The emulator could not access this ROM path.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val EXTRA_ROM = "ROM"
    }
}

abstract class FilePathLauncher(
    override val packageName: String,
    private val action: String = Intent.ACTION_VIEW,
    private val mimeType: String = "application/octet-stream",
) : EmulatorLauncher {
    override fun createLaunchIntent(context: Context, game: Game): Intent {
        val fileUri = game.primaryFile.contentUri ?: Uri.parse("file://${game.primaryFile.path}")
        return Intent(action).apply {
            setPackage(packageName)
            setDataAndType(fileUri, mimeType)
            putExtra(EmulatorLauncher.EXTRA_ROM, game.primaryFile.path)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

class RetroArchLauncher : FilePathLauncher("com.retroarch") {
    override fun createLaunchIntent(context: Context, game: Game): Intent =
        super.createLaunchIntent(context, game).apply {
            putExtra("LIBRETRO", game.platform.romFolderName)
            putExtra("CONFIGFILE", "/storage/emulated/0/Android/data/com.retroarch/files/retroarch.cfg")
        }
}

class DolphinLauncher : FilePathLauncher("org.dolphinemu.dolphinemu")
class CemuLauncher : FilePathLauncher("info.cemu.Cemu")
class MelonDsLauncher : FilePathLauncher("me.magnum.melonds")
class AzaharLauncher : FilePathLauncher("io.github.lime3ds.android")
class EdenLauncher : FilePathLauncher("dev.eden.eden_emulator")
class NetherSx2Launcher : FilePathLauncher("xyz.aethersx2.android")
class Aps3eLauncher : FilePathLauncher("com.aps3e.android")
class PpssppLauncher : FilePathLauncher("org.ppsspp.ppsspp")
class EmuCoreVLauncher : FilePathLauncher("com.emucore.psvita")
class FlycastLauncher : FilePathLauncher("com.flycast.emulator")
