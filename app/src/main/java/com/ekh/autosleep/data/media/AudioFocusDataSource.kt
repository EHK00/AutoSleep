package com.ekh.autosleep.data.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android [AudioManager]를 통해 AudioFocus를 요청/반환하는 데이터 소스.
 * [MediaControlRepositoryImpl]에서 미디어 일시정지 보조 수단으로 사용된다.
 * AudioFocus를 획득하면 재생 중인 앱이 스스로 일시정지할 수 있다 (앱이 AudioFocus를 무시하면 효과 없음).
 */
@Singleton
class AudioFocusDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** 마지막으로 요청한 [AudioFocusRequest]. [abandonFocus] 호출 시 반환에 사용된다. */
    private var focusRequest: AudioFocusRequest? = null

    /**
     * USAGE_MEDIA 타입의 AudioFocus GAIN을 요청한다.
     * 시스템이 즉시 포커스를 부여하지 않으면 [Result.failure]를 반환한다.
     */
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

    /** 획득한 AudioFocus를 시스템에 반환한다. 이전 재생 앱이 포커스를 다시 얻을 수 있다. */
    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
