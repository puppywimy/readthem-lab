package com.back.domain.tag.service

import com.back.domain.tag.entity.Tag
import com.back.domain.tag.repository.TagRepository
import com.back.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TagService(
    private val tagRepository: TagRepository
) {

    @Transactional
    fun createTag(name: String) {
        if (tagRepository.findByName(name) != null)
            throw ServiceException("409-1", "이미 존재하는 태그입니다.")

        tagRepository.save(Tag(name))
    }

    @Transactional
    fun findByNameOrSave(name: String): Tag =
        tagRepository.findByName(name) ?: tagRepository.save(Tag(name))
}
