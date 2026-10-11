package com.back.domain.review.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidTagsValidator : ConstraintValidator<ValidTags, List<String>> {
    private var value: List<String>? = null

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true

        this.value = value

        return this.isMaxSize5 && this.isAllTagValid && this.isAllTagUnique
    }

    private val isMaxSize5: Boolean
        get() = value!!.size <= 5

    private val isAllTagValid: Boolean
        get() {
            for (i in value!!.indices) {
                val tag = value!![i]
                if (tag.length in 1..30) continue
                return false
            }

            return true
        }

    private val isAllTagUnique: Boolean
        get() {
            val tagCounts: MutableMap<String, Int> =
                mutableMapOf()

            for (i in value!!.indices) {
                val tag = value!![i]
                tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
            }

            for (count in tagCounts.values) {
                if (count <= 1) continue
                return false
            }

            return true
        }
}