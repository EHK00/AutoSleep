package com.ekh.autosleep.domain.usecase.screen

import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class LockScreenUseCaseTest {

    private val screenControlRepository: ScreenControlRepository = mockk()
    private val permissionRepository: PermissionRepository = mockk()
    private val useCase = LockScreenUseCase(screenControlRepository, permissionRepository)

    @Test
    fun `접근성 권한이 있으면 lockScreen을 사용한다`() = runTest {
        every { permissionRepository.getPermissionState() } returns permissionState(
            accessibilityGranted = true,
        )
        coEvery { screenControlRepository.lockScreen() } returns Result.success(Unit)

        val result = useCase()

        coVerify(exactly = 1) { screenControlRepository.lockScreen() }
        coVerify(exactly = 0) { screenControlRepository.shortenScreenTimeout() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `접근성 없고 Device Admin 권한이 있으면 lockScreen을 사용한다`() = runTest {
        every { permissionRepository.getPermissionState() } returns permissionState(
            deviceAdminGranted = true,
        )
        coEvery { screenControlRepository.lockScreen() } returns Result.success(Unit)

        val result = useCase()

        coVerify(exactly = 1) { screenControlRepository.lockScreen() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `접근성과 Device Admin이 없고 WRITE_SETTINGS만 있으면 타임아웃 단축을 사용한다`() = runTest {
        every { permissionRepository.getPermissionState() } returns permissionState(
            writeSettingsGranted = true,
        )
        coEvery { screenControlRepository.shortenScreenTimeout() } returns Result.success(Unit)

        val result = useCase()

        coVerify(exactly = 1) { screenControlRepository.shortenScreenTimeout() }
        coVerify(exactly = 0) { screenControlRepository.lockScreen() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `아무 권한도 없으면 failure를 반환한다`() = runTest {
        every { permissionRepository.getPermissionState() } returns permissionState()

        val result = useCase()

        assertTrue(result.isFailure)
    }

    private fun permissionState(
        notificationListenerGranted: Boolean = false,
        accessibilityGranted: Boolean = false,
        deviceAdminGranted: Boolean = false,
        writeSettingsGranted: Boolean = false,
    ) = PermissionState(
        notificationListenerGranted = notificationListenerGranted,
        accessibilityGranted = accessibilityGranted,
        deviceAdminGranted = deviceAdminGranted,
        writeSettingsGranted = writeSettingsGranted,
    )
}
