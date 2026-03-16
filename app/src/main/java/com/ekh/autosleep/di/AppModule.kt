package com.ekh.autosleep.di

import com.ekh.autosleep.data.media.MediaControlRepositoryImpl
import com.ekh.autosleep.data.permission.PermissionRepositoryImpl
import com.ekh.autosleep.data.screen.ScreenControlRepositoryImpl
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
import com.ekh.autosleep.service.TimerServiceController
import com.ekh.autosleep.service.TimerServiceControllerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindTimerRepository(impl: TimerRepositoryImpl): TimerRepository

    @Binds @Singleton
    abstract fun bindMediaControlRepository(impl: MediaControlRepositoryImpl): MediaControlRepository

    @Binds @Singleton
    abstract fun bindScreenControlRepository(impl: ScreenControlRepositoryImpl): ScreenControlRepository

    @Binds @Singleton
    abstract fun bindPermissionRepository(impl: PermissionRepositoryImpl): PermissionRepository

    @Binds @Singleton
    abstract fun bindTimerServiceController(impl: TimerServiceControllerImpl): TimerServiceController
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton
    fun provideStartTimerUseCase(repo: TimerRepository) = StartTimerUseCase(repo)

    @Provides @Singleton
    fun provideCancelTimerUseCase(repo: TimerRepository) = CancelTimerUseCase(repo)

    @Provides @Singleton
    fun providePauseAllMediaUseCase(repo: MediaControlRepository) = PauseAllMediaUseCase(repo)

    @Provides @Singleton
    fun provideRequestAudioFocusUseCase(repo: MediaControlRepository) = RequestAudioFocusUseCase(repo)

    @Provides @Singleton
    fun provideLockScreenUseCase(screenRepo: ScreenControlRepository) = LockScreenUseCase(screenRepo)

    @Provides @Singleton
    fun provideCheckPermissionsUseCase(repo: PermissionRepository) = CheckPermissionsUseCase(repo)

    @Provides @Singleton
    fun provideExecuteSleepSequenceUseCase(
        pauseAllMedia: PauseAllMediaUseCase,
        requestAudioFocus: RequestAudioFocusUseCase,
        lockScreen: LockScreenUseCase,
    ) = ExecuteSleepSequenceUseCase(pauseAllMedia, requestAudioFocus, lockScreen)
}
