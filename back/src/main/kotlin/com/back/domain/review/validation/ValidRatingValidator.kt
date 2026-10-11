package com.back.domain.review.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidRatingValidator : ConstraintValidator<ValidRating, Float> {

    override fun isValid(value: Float?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true

        val isInRange = value in 0f..5f
        val isHalf = value * 2f % 1f == 0f

        return isInRange && isHalf
    }
}