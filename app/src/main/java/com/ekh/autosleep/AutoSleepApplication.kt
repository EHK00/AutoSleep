package com.ekh.autosleep

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt 의존성 주입을 초기화하는 Application 클래스.
 * [@HiltAndroidApp]이 Hilt 컴포넌트 생성 코드를 자동 생성하며,
 * AndroidManifest.xml의 `android:name`에 등록되어야 한다.
 */
@HiltAndroidApp
class AutoSleepApplication : Application()
