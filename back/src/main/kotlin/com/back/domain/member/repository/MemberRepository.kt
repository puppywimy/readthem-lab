package com.back.domain.member.repository

import com.back.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByRefreshToken(apiKey: String): Member?

    fun findByUsername(username: String): Member?

    fun findByGithubId(githubId: String): Member?
}