package com.calico.launcher

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class CalicoSfxPlayer(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = buildMap {
        SFX_FILES.forEach { file ->
            val key = file.substringBeforeLast('.')
            put(key, soundPool.load(context.assets.openFd("sfx/$file"), 1))
        }
    }

    fun navigate() = play("navigate")
    fun gameSelect() = play("game_select")
    fun menuSelect() = play("select_menu")
    fun frontendLaunch() = play("frontend_launch")
    fun error() = play("error")
    fun retroAchievements() = play("retroachievements")
    fun xyButtonOpen() = play("xy_button_open")
    fun xyButtonClose() = play("xy_button_close")
    fun screenSwap() = play("screen_swap")
    fun folderOpen() = play("folder_open")
    fun folderClose() = play("folder_close")
    fun back() = play("back")

    fun release() {
        soundPool.release()
    }

    private fun play(key: String) {
        soundIds[key]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    private companion object {
        val SFX_FILES = listOf(
            "navigate.mp3",
            "game_select.mp3",
            "select_menu.mp3",
            "frontend_launch.mp3",
            "error.wav",
            "retroachievements.mp3",
            "xy_button_close.mp3",
            "xy_button_open.mp3",
            "screen_swap.mp3",
            "folder_close.mp3",
            "folder_open.mp3",
            "back.wav",
        )
    }
}
