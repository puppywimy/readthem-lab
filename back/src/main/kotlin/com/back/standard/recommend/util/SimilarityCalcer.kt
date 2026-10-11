package com.back.standard.recommend.util

interface SimilarityCalcer<I, L> {

    data class Similar<I>(
        val id: I,
        val score: Double
    )

    fun setVectorA(vector: Vector<L>)
    fun setVectorB(vector: Vector<L>)
    fun getSimilarity(): Double

    fun getSimilarList(target: Vector<L>, matrix: Map<I, Vector<L>>): List<Similar<I>>
}
