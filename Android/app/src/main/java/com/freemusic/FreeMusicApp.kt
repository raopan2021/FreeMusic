package com.freemusic

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FreeMusicApp : Application() {
    
    var currentPlayer: ExoPlayer? = null
    
    override fun onTerminate() {
        super.onTerminate()
        currentPlayer?.release()
        currentPlayer = null
    }
}
