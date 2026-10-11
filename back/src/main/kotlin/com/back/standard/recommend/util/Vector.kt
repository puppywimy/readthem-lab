package com.back.standard.recommend.util

class Vector<L> {

    data class VectorElement<L>(
        val label: L,
        val value: Double
    )

    private val values = mutableMapOf<L, Double>()
    private var sum = 0.0

    fun getLabels(): Set<L> = values.keys

    fun getVectorElementList(): List<VectorElement<L>> =
        values.entries.map { VectorElement(it.key, it.value) }

    fun getValue(label: L): Double? = values[label]

    fun putValue(label: L, value: Double) {
        values[label]?.let { sum -= it }
        values[label] = value
        sum += value
    }

    fun subtractionValue(value: Double): Vector<L> {
        values.replaceAll { _, oldValue -> oldValue - value }
        sum -= value * values.size
        return this
    }

    fun getAverageValue(): Double = sum / values.size

    fun isEmpty(): Boolean = values.isEmpty()

    companion object {
        @JvmStatic
        fun <L> hadamardProduct(v1: Vector<L>, v2: Vector<L>): Vector<L> {
            val v = Vector<L>()

            v1.values.forEach { (key, oldValue) ->
                val other = v2.values[key] ?: return@forEach
                v.putValue(key, oldValue * other)
            }

            return v
        }
    }
}
