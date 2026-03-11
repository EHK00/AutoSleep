package com.ekh.autosleep.data.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    fun requestFocus(): Result<Unit> = runCatching {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener {}
            .build()

        focusRequest = request
        val result = audioManager.requestAudioFocus(request)
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            error("AudioFocus request not granted: $result")
        }
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
