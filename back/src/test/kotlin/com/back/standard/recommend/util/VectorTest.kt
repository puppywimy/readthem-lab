package com.back.standard.recommend.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VectorTest {

    @Test
    fun `subtractionValue updates average value`() {
        val vector = Vector<String>()
        vector.putValue("a", 2.0)
        vector.putValue("b", 4.0)

        vector.subtractionValue(1.0)

        assertThat(vector.getAverageValue()).isEqualTo(2.0)
    }
}
