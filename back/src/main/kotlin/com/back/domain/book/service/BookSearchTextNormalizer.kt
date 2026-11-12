package com.back.domain.book.service

/**
 * 도서 검색어 전처리.
 *
 * `book.search_text` 생성 컬럼(V2__add_book_search_text.sql)과 같은 규칙을 적용해야 한다.
 * 두 규칙이 어긋나면 검색 결과가 조용히 빠지므로, 바꿀 때는 마이그레이션과 함께 고치고
 * BookSearchTextNormalizerRuleTest로 확인한다.
 *
 * 1. 기호 7개(`+ # & . : - /`)를 ngram이 글자로 취급하는 대체 문자로 바꾼다.
 * 2. 글자(`\p{L}`)·숫자(`\p{Nd}`)가 아닌 나머지 문자(공백 포함)를 모두 지운다.
 *
 * 대소문자는 바꾸지 않는다. 비교는 컬럼 콜레이션(`utf8mb4_0900_ai_ci`)이 대소문자를 무시한다.
 */
object BookSearchTextNormalizer {

    val SYMBOL_REPLACEMENTS: List<Pair<Char, Char>> = listOf(
        '+' to 'ǂ',
        '#' to 'ǁ',
        '&' to 'ǀ',
        '.' to 'ǃ',
        ':' to 'ƻ',
        '-' to 'ʔ',
        '/' to 'ƾ',
    )

    private val NON_LETTER_OR_DIGIT = Regex("[^\\p{L}\\p{Nd}]")

    fun normalize(text: String): String {
        val replaced = SYMBOL_REPLACEMENTS.fold(text) { acc, (symbol, replacement) ->
            acc.replace(symbol, replacement)
        }

        return NON_LETTER_OR_DIGIT.replace(replaced, "")
    }
}
