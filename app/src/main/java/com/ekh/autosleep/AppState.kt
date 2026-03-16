package com.ekh.autosleep

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 모든 component를 아우르는 상태 객체
 */
@Singleton
class AppState @Inject constructor() {
    /**
     * 앱이 현재 포그라운드(Activity 표시 중)인지 추적하는 싱글톤 상태.
     * [MainActivity]가 onResume/onPause에서 갱신하고,
     * [com.ekh.autosleep.service.TimerService]가 구독해 알림 표시 여부를 결정한다.
     */
    val isInForeground = MutableStateFlow(false)
}
