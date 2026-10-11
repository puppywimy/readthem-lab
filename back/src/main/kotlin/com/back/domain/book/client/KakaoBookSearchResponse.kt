package com.back.domain.book.client

class KakaoBookSearchResponse(
    val documents: List<KakaoBookDocument> = emptyList(),
)

class KakaoBookDocument(
    val thumbnail: String? = null,
)