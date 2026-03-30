package com.ekh.autosleep.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.ekh.autosleep.data.analytics.TimerLogRepositoryImpl
import com.ekh.autosleep.data.analytics.db.TimerLogDao
import com.ekh.autosleep.data.media.MediaControlRepositoryImpl
import com.ekh.autosleep.data.permission.PermissionRepositoryImpl
import com.ekh.autosleep.data.preset.TimerPresetRepositoryImpl
import com.ekh.autosleep.data.routine.RoutineRepositoryImpl
import com.ekh.autosleep.data.routine.db.MIGRATION_1_2
import com.ekh.autosleep.data.routine.db.RoutineDao
import com.ekh.autosleep.data.routine.db.RoutineDatabase
import com.ekh.autosleep.data.screen.ScreenControlRepositoryImpl
import com.ekh.autosleep.data.settings.SettingsRepository
import com.ekh.autosleep.data.settings.SettingsRepositoryImpl
import com.ekh.autosleep.data.timer.TimerRepositoryImpl
import com.ekh.autosleep.domain.repository.MediaControlRepository
import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.RoutineRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository
import com.ekh.autosleep.domain.repository.TimerLogRepository
import com.ekh.autosleep.domain.repository.TimerPresetRepository
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.analytics.GetTimerLogsUseCase
import com.ekh.autosleep.domain.usecase.analytics.RecordTimerLogUseCase
import com.ekh.autosleep.domain.usecase.media.PauseAllMediaUseCase
import com.ekh.autosleep.domain.usecase.media.RequestAudioFocusUseCase
import com.ekh.autosleep.domain.usecase.permission.CheckPermissionsUseCase
import com.ekh.autosleep.domain.usecase.routine.AddRoutineUseCase
import com.ekh.autosleep.domain.usecase.routine.DeleteRoutineUseCase
import com.ekh.autosleep.domain.usecase.routine.GetRoutinesUseCase
import com.ekh.autosleep.domain.usecase.routine.ToggleRoutineUseCase
import com.ekh.autosleep.domain.usecase.routine.UpdateRoutineUseCase
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.timerPresetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timer_presets",
)

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

    @Binds @Singleton
    abstract fun bindTimerPresetRepository(impl: TimerPresetRepositoryImpl): TimerPresetRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds @Singleton
    abstract fun bindTimerLogRepository(impl: TimerLogRepositoryImpl): TimerLogRepository
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
    fun provideTimerPresetDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.timerPresetDataStore

    @Provides @Singleton
    fun provideExecuteSleepSequenceUseCase(
        pauseAllMedia: PauseAllMediaUseCase,
        requestAudioFocus: RequestAudioFocusUseCase,
        lockScreen: LockScreenUseCase,
    ) = ExecuteSleepSequenceUseCase(pauseAllMedia, requestAudioFocus, lockScreen)

    @Provides @Singleton
    fun provideRoutineDatabase(@ApplicationContext context: Context): RoutineDatabase =
        Room.databaseBuilder(context, RoutineDatabase::class.java, "routine_db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides @Singleton
    fun provideRoutineDao(db: RoutineDatabase): RoutineDao = db.routineDao()

    @Provides @Singleton
    fun provideTimerLogDao(db: RoutineDatabase): TimerLogDao = db.timerLogDao()

    @Provides @Singleton
    fun provideGetTimerLogsUseCase(repo: TimerLogRepository) = GetTimerLogsUseCase(repo)

    @Provides @Singleton
    fun provideRecordTimerLogUseCase(repo: TimerLogRepository) = RecordTimerLogUseCase(repo)

    @Provides @Singleton
    fun provideGetRoutinesUseCase(repo: RoutineRepository) = GetRoutinesUseCase(repo)

    @Provides @Singleton
    fun provideAddRoutineUseCase(repo: RoutineRepository) = AddRoutineUseCase(repo)

    @Provides @Singleton
    fun provideUpdateRoutineUseCase(repo: RoutineRepository) = UpdateRoutineUseCase(repo)

    @Provides @Singleton
    fun provideDeleteRoutineUseCase(repo: RoutineRepository) = DeleteRoutineUseCase(repo)

    @Provides @Singleton
    fun provideToggleRoutineUseCase(repo: RoutineRepository) = ToggleRoutineUseCase(repo)
}
