package com.back.standard.recommend.util

import kotlin.math.sqrt

class CosineSimilarityCalcer<I, L> : SimilarityCalcer<I, L> {

    private var vectorASqrMagnitude = 0.0
    private var vectorBSqrMagnitude = 0.0

    private var vectorA: Vector<L>? = null
    private var vectorB: Vector<L>? = null

    override fun setVectorA(vector: Vector<L>) {
        vectorA = vector
        vectorASqrMagnitude = getSqrMagnitude(vector.getVectorElementList())
    }

    override fun setVectorB(vector: Vector<L>) {
        vectorB = vector
        vectorBSqrMagnitude = getSqrMagnitude(vector.getVectorElementList())
    }

    override fun getSimilarity(): Double {
        val a = vectorA ?: return 0.0
        val b = vectorB ?: return 0.0

        var ret = 0.0

        a.getVectorElementList().forEach { element ->
            val otherValue = b.getValue(element.label) ?: return@forEach
            ret += element.value * otherValue
        }

        val denominator = sqrt(vectorASqrMagnitude * vectorBSqrMagnitude)

        if (denominator == 0.0) return 0.0

        return ret / denominator
    }

    override fun getSimilarList(target: Vector<L>, matrix: Map<I, Vector<L>>): List<SimilarityCalcer.Similar<I>> {
        setVectorA(target)

        return matrix.entries
            .filter { it.value !== target && !it.value.isEmpty() }
            .map { entry ->
                setVectorB(entry.value)
                SimilarityCalcer.Similar(entry.key, getSimilarity())
            }
            .sortedByDescending { it.score }
    }

    companion object {
        private fun <L> getSqrMagnitude(elements: List<Vector.VectorElement<L>>): Double =
            elements.sumOf { it.value * it.value }
    }
}
