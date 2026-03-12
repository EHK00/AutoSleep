package com.ekh.autosleep.domain.entity

/**
 * [ExecuteSleepSequenceUseCase] 실행 결과를 나타내는 sealed class.
 * 미디어 일시정지 및 화면 잠금 각각의 성공 여부를 세분화하여 표현한다.
 */
sealed class SleepResult {
    /**
     * 미디어 일시정지와 화면 잠금이 모두 성공한 경우.
     * @property sessionsPaused 일시정지된 미디어 세션 수.
     */
    data class Success(val sessionsPaused: Int) : SleepResult()

    /**
     * 미디어 일시정지 또는 화면 잠금 중 하나가 실패한 부분 성공 상태.
     * @property sessionsPaused 일시정지된 미디어 세션 수.
     * @property screenLocked 화면 잠금 성공 여부.
     * @property error 실패한 단계의 오류 메시지.
     */
    data class PartialSuccess(
        val sessionsPaused: Int,
        val screenLocked: Boolean,
        val error: String,
    ) : SleepResult()

    /**
     * 수면 시퀀스 자체가 실행 불가능한 경우 (예: 권한 없음).
     * @property error 실패 원인 메시지.
     */
    data class Failure(val error: String) : SleepResult()
}
