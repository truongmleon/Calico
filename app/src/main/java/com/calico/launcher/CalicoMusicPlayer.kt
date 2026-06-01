package com.calico.launcher

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.calico.launcher.model.MusicTrack
import java.io.File

class CalicoMusicPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var currentTrackId: Int? = null

    fun play(track: MusicTrack, loop: Boolean) {
        val uri = track.contentUri ?: track.filePath?.let { Uri.fromFile(File(it)) } ?: return
        if (currentTrackId == track.id) {
            player?.isLooping = loop
            if (player?.isPlaying != true) {
                player?.start()
            }
            return
        }
        release()
        player = MediaPlayer().apply {
            setDataSource(appContext, uri)
            isLooping = loop
            prepare()
            start()
        }
        currentTrackId = track.id
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.start()
    }

    fun setLooping(loop: Boolean) {
        player?.isLooping = loop
    }

    fun release() {
        player?.release()
        player = null
        currentTrackId = null
    }
}
