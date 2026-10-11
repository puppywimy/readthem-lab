package com.back.domain.book.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Repository
class BookViewCountRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val minuteKey: String
        get() = "viewCount:minute:%s".format(
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            )
        )

    fun findBookViewCountById(bookId: Long): Int? {
        try {
            val score = redisTemplate.opsForZSet()
                .score("viewCount", bookId.toString())

            if (score != null) {
                return score.toInt()
            }
        } catch (_: Exception) {

        }

        return null;

    }

    fun findBookIdOrderByTopViewedInLastHout(page: Int, size: Int): List<Long>? {

        try {
            val start = page * size
            val end = start + size - 1

            val now = LocalDateTime.now()

            val keys: MutableList<String> = mutableListOf()

            for (i in 0..59) {
                val key: String = "viewCount:minute:%s".format(
                    now.minusMinutes(i.toLong())
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                )

                keys.add(key)
            }

            val tempKey = "viewCount:lastHour:temp:" + UUID.randomUUID()

            redisTemplate.delete(tempKey)

            redisTemplate.opsForZSet().unionAndStore(
                keys[0],
                keys.subList(1, keys.size),
                tempKey
            )

            redisTemplate.expire(tempKey, Duration.ofSeconds(10))

            val bookIds = redisTemplate.opsForZSet()
                .reverseRange(tempKey, start.toLong(), end.toLong())

            if (bookIds == null || bookIds.isEmpty()) {
                return listOf()
            }

            return bookIds
                .map { bookId: String -> bookId.toLong() }

        } catch (_: Exception) {
            return null;
        }
    }

    fun tryIncreaseViewAtRedis(bookId: Long, lambda: () -> Int): Boolean {
        try {

            if (redisTemplate.opsForZSet().score("viewCount", bookId.toString()) == null) {
                redisTemplate.opsForZSet().add(
                    "viewCount", bookId.toString(), lambda().toDouble()
                )
            }

            redisTemplate.opsForZSet().incrementScore("viewCount", bookId.toString(), 1.0)

            val minuteKey = this.minuteKey
            redisTemplate.opsForZSet().incrementScore(
                minuteKey, bookId.toString(), 1.0
            )
            redisTemplate.expire(minuteKey, Duration.ofMinutes(70))

            return true;

        } catch (_: Exception) {
            return false;
        }
    }

    fun findAllBookViews(): Map<Long, Int> {
        val viewMap = mutableMapOf<Long, Int>();

        try {
            val tuples: MutableSet<ZSetOperations.TypedTuple<String>>? =
                redisTemplate.opsForZSet().rangeWithScores("viewCount", 0, -1)

            if (tuples.isNullOrEmpty()) return viewMap;

            for (tuple in tuples) {
                if (tuple.getValue() == null || tuple.score == null) continue

                val bookId = tuple.getValue()?.toLong()
                val viewCount = tuple.score?.toInt()

                if (bookId != null && viewCount != null) {
                    viewMap[bookId] = viewCount;
                }
            }
        } catch (_: Exception) {
            // log.warn(...)
        }

        return viewMap;
    }

}