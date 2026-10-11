package com.back.domain.review.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidTagsValidator::class])
annotation class ValidTags(
    val message: String = "tags는 최대 5개, 각 tag는 서로 중복되지 않고 1~20자여야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)