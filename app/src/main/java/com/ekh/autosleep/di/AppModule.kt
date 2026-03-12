package com.ekh.autosleep.di

import com.ekh.autosleep.data.media.AudioFocusDataSource
import com.ekh.autosleep.data.media.MediaControlRepositoryImpl
import com.ekh.autosleep.data.permission.PermissionRepositoryImpl
import com.ekh.autosleep.data.screen.AccessibilityScreenSource
import com.ekh.autosleep.data.screen.DeviceAdminScreenSource
import com.ekh.autosleep.data.screen.ScreenControlRepositoryImpl
import com.ekh.autosleep.data.screen.ScreenTimeoutSource
import com.ekh.autosleep.data.timer.TimerRepositoryImpl
import com.ekh.autosleep.domain.repository.MediaControlRepository
import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.media.PauseAllMediaUseCase
import com.ekh.autosleep.domain.usecase.media.RequestAudioFocusUseCase
import com.ekh.autosleep.domain.usecase.permission.CheckPermissionsUseCase
import com.ekh.autosleep.domain.usecase.screen.LockScreenUseCase
import com.ekh.autosleep.domain.usecase.sleep.ExecuteSleepSequenceUseCase
import com.ekh.autosleep.domain.usecase.timer.CancelTimerUseCase
import com.ekh.autosleep.domain.usecase.timer.StartTimerUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 인터페이스와 구현체를 바인딩하는 Hilt 모듈.
 * [@Binds]를 사용하여 인터페이스 타입으로 구현체를 주입받을 수 있도록 등록한다.
 * [SingletonComponent]에 설치되어 앱 전역에서 단일 인스턴스를 공유한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** [TimerRepositoryImpl]을 [TimerRepository]로 바인딩한다. */
    @Binds @Singleton
    abstract fun bindTimerRepository(impl: TimerRepositoryImpl): TimerRepository

    /** [MediaControlRepositoryImpl]을 [MediaControlRepository]로 바인딩한다. */
    @Binds @Singleton
    abstract fun bindMediaControlRepository(impl: MediaControlRepositoryImpl): MediaControlRepository

    /** [ScreenControlRepositoryImpl]을 [ScreenControlRepository]로 바인딩한다. */
    @Binds @Singleton
    abstract fun bindScreenControlRepository(impl: ScreenControlRepositoryImpl): ScreenControlRepository

    /** [PermissionRepositoryImpl]을 [PermissionRepository]로 바인딩한다. */
    @Binds @Singleton
    abstract fun bindPermissionRepository(impl: PermissionRepositoryImpl): PermissionRepository
}

/**
 * Use Case 인스턴스를 생성하여 제공하는 Hilt 모듈.
 * Use Case는 constructor injection을 직접 지원하지 않으므로 [@Provides]로 수동 생성한다.
 * [SingletonComponent]에 설치되어 앱 전역에서 단일 인스턴스를 공유한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /** [StartTimerUseCase]를 생성하여 제공한다. */
    @Provides @Singleton
    fun provideStartTimerUseCase(repo: TimerRepository) = StartTimerUseCase(repo)

    /** [CancelTimerUseCase]를 생성하여 제공한다. */
    @Provides @Singleton
    fun provideCancelTimerUseCase(repo: TimerRepository) = CancelTimerUseCase(repo)

    /** [PauseAllMediaUseCase]를 생성하여 제공한다. */
    @Provides @Singleton
    fun providePauseAllMediaUseCase(repo: MediaControlRepository) = PauseAllMediaUseCase(repo)

    /** [RequestAudioFocusUseCase]를 생성하여 제공한다. */
    @Provides @Singleton
    fun provideRequestAudioFocusUseCase(repo: MediaControlRepository) = RequestAudioFocusUseCase(repo)

    /** [LockScreenUseCase]를 생성하여 제공한다. ScreenControlRepository와 PermissionRepository 모두 필요. */
    @Provides @Singleton
    fun provideLockScreenUseCase(
        screenRepo: ScreenControlRepository,
        permissionRepo: PermissionRepository,
    ) = LockScreenUseCase(screenRepo, permissionRepo)

    /** [CheckPermissionsUseCase]를 생성하여 제공한다. */
    @Provides @Singleton
    fun provideCheckPermissionsUseCase(repo: PermissionRepository) = CheckPermissionsUseCase(repo)

    /** [ExecuteSleepSequenceUseCase]를 생성하여 제공한다. 미디어, 오디오 포커스, 화면 잠금 Use Case를 조합한다. */
    @Provides @Singleton
    fun provideExecuteSleepSequenceUseCase(
        pauseAllMedia: PauseAllMediaUseCase,
        requestAudioFocus: RequestAudioFocusUseCase,
        lockScreen: LockScreenUseCase,
    ) = ExecuteSleepSequenceUseCase(pauseAllMedia, requestAudioFocus, lockScreen)
}
