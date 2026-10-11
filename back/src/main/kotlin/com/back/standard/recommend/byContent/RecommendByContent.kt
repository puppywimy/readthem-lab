package com.back.standard.recommend.byContent

import com.back.standard.recommend.util.CosineSimilarityCalcer
import com.back.standard.recommend.util.SimilarityCalcer
import com.back.standard.recommend.util.Vector
import kotlin.math.ln

class RecommendByContent(
    private val calcer: SimilarityCalcer<Long, Long> = CosineSimilarityCalcer()
) {

    data class Content(
        val id: Long,
        val content: String
    )

    private val tfMap = mutableMapOf<Long, Vector<Long>>()
    private val idfMap = mutableMapOf<Long, Int>()

    private var totalCnt = 0L

    private fun getTFVector(content: String): Vector<Long> {
        totalCnt++
        val words = content.split(" ")

        val wordMap = mutableMapOf<Long, Int>()

        for (word in words) {
            val wordHash = word.hashCode().toLong()
            if (wordMap.containsKey(wordHash)) {
                wordMap[wordHash] = wordMap.getValue(wordHash) + 1
                continue
            }
            wordMap[wordHash] = 1
            idfMap[wordHash] = idfMap.getOrDefault(wordHash, 0) + 1
        }

        val ret = Vector<Long>()

        wordMap.forEach { (key, value) ->
            ret.putValue(key, value.toDouble() / wordMap.size)
        }

        return ret
    }

    fun setData(contents: List<Content>) {
        contents.forEach { content ->
            if (tfMap.containsKey(content.id)) return@forEach
            tfMap[content.id] = getTFVector(content.content)
        }
    }

    fun getRecommendList(id: Long, maxRecommends: Int): List<Long> {
        if (!tfMap.containsKey(id)) return emptyList()

        val idf = Vector<Long>()

        idfMap.forEach { (key, value) ->
            idf.putValue(key, ln(totalCnt.toDouble() / value))
        }

        val tfIdf = mutableMapOf<Long, Vector<Long>>()

        tfMap.forEach { (key, value) ->
            tfIdf[key] = Vector.hadamardProduct(value, idf)
        }

        val similarList = calcer.getSimilarList(tfIdf.getValue(id), tfIdf)

        return similarList.take(maxRecommends).map { it.id }
    }
}
