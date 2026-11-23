package com.back.domain.book.client

// 카카오 API에 표지가 없는 것과 호출에 실패한 것을 구분한다.
// 표지가 없으면 조회 이력을 남겨 재호출하지 않고, 실패는 다음 조회 때 다시 시도한다.
sealed interface KakaoThumbnailResult {
    data class Found(val url: String) : KakaoThumbnailResult
    data object NotFound : KakaoThumbnailResult
    data object Failed : KakaoThumbnailResult
}
