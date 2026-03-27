package com.ekh.autosleep.presentation.permission

import androidx.lifecycle.ViewModel
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.usecase.permission.CheckPermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 권한 화면의 상태를 관리하는 ViewModel.
 * [permissionState]를 UI에 노출하며, [refreshPermissions] 호출 시 현재 권한 상태를 재조회한다.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val checkPermissions: CheckPermissionsUseCase,
) : ViewModel() {

    private val _permissionState = MutableStateFlow(checkPermissions())

    /** 현재 권한 허용 상태 스트림. [refreshPermissions] 호출 시 갱신된다. */
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    /**
     * 권한 상태를 현재 시점으로 다시 조회하여 갱신한다.
     * 설정 화면에서 돌아올 때([Lifecycle.Event.ON_RESUME]) UI가 호출한다.
     */
    fun refreshPermissions() {
        _permissionState.value = checkPermissions()
    }
}
