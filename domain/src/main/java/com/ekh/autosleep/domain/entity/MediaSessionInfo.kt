package com.ekh.autosleep.domain.entity

/**
 * 현재 활성화된 미디어 세션 하나를 나타내는 불변 데이터 클래스.
 * [MediaControlRepository]를 통해 조회되며, 어떤 앱이 재생 중인지 식별하는 데 사용된다.
 *
 * @property packageName 미디어 세션을 소유한 앱의 패키지명 (예: "com.google.android.youtube").
 * @property sessionTag 세션을 구분하기 위한 선택적 태그. null일 수 있다.
 */
data class MediaSessionInfo(
    val packageName: String,
    val sessionTag: String?,
)
