package com.back.standard.recommend.byRating

import com.back.standard.recommend.util.CosineSimilarityCalcer
import com.back.standard.recommend.util.SimilarityCalcer
import com.back.standard.recommend.util.Vector

class SimilarityRecommendByRating<R, S>(
    private val calcer: SimilarityCalcer<R, S> = CosineSimilarityCalcer()
) {

    data class Rating<R, S>(
        val reviewerId: R,
        val subjectId: S,
        val rating: Float
    )

    private val ratingMatrix = mutableMapOf<R, Vector<S>>()

    fun clear() {
        ratingMatrix.clear()
    }

    fun setData(reviews: List<Rating<R, S>>) {
        reviews.forEach { review ->
            ratingMatrix.getOrPut(review.reviewerId) { Vector() }
                .putValue(review.subjectId, review.rating.toDouble())
        }
    }

    fun getRecommendList(targetUserId: R, referenceCnt: Int, maxRecommend: Int): List<S> {
        val targetUser = ratingMatrix[targetUserId] ?: return emptyList()
        if (targetUser.isEmpty()) return emptyList()

        ratingMatrix.values.forEach { it.subtractionValue(it.getAverageValue()) }

        val similarList = calcer.getSimilarList(targetUser, ratingMatrix)

        val recommends = mutableMapOf<S, Double>()
        val alreadyRead = targetUser.getLabels()

        for (i in 0 until minOf(referenceCnt, similarList.size)) {
            val similarScore = similarList[i].score

            val compareReviewList = ratingMatrix.getValue(similarList[i].id).getVectorElementList()

            compareReviewList
                .filter { it.label !in alreadyRead }
                .forEach { rating ->
                    val score = rating.value * similarScore
                    val existing = recommends[rating.label]
                    if (existing != null && existing <= score) return@forEach
                    recommends[rating.label] = score
                }
        }

        return recommends.entries
            .sortedByDescending { it.value }
            .take(maxRecommend)
            .map { it.key }
    }
}
