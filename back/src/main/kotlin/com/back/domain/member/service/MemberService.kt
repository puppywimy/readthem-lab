package com.back.domain.member.service

import com.back.domain.member.dto.AdminMemberDto
import com.back.domain.member.dto.MemberDto
import com.back.domain.member.dto.MemberWithUsernameAndWidgetLinkDto
import com.back.domain.member.entity.Member
import com.back.domain.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.NoSuchElementException

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    fun getMemberById(id: Long): Member {

        val member = memberRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("존재하지 않는 회원입니다.")

        if (member.isDeleted) {
            throw NoSuchElementException("존재하지 않는 회원입니다.")
        }

        return member
    }

    fun getMemberByUsername(username: String): Member {
        val member = memberRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("존재하지 않는 회원입니다.")

        if (member.isDeleted) {
            throw ServiceException("404-1", "존재하지 않는 회원입니다.")
        }

        return member
    }

    fun getMember(id: Long): MemberDto {
        return MemberDto(getMemberById(id))
    }

    fun getMy(member: Member): MemberWithUsernameAndWidgetLinkDto {
        return MemberWithUsernameAndWidgetLinkDto(getMemberById(member.id))
    }

    fun getMemberByRefreshToken(apiKey: String): Member? =
        memberRepository.findByRefreshToken(apiKey)

    @Transactional
    fun join(username: String, password: String, githubId: String, imgUrl: String?): Member =
        join(username, password, githubId, githubId, imgUrl)

    @Transactional
    fun join(username: String, password: String, githubId: String?, nickname: String, imgUrl: String?): Member {

        memberRepository.findByUsername(username)?.let{
            throw ServiceException("409-1", "이미 존재하는 아이디입니다.")
        }

        githubId?.let {
            memberRepository.findByGithubId(githubId)?.let {
                throw ServiceException("409-2", "이미 존재하는 githubId입니다.")
            }
        }

        return memberRepository.save(
            Member(
                username = username,
                password = encodePasswordImplementation(password),
                githubId = githubId,
                nickname = nickname,
                imgUrl = imgUrl
            )
        )
    }

    @Transactional
    fun deleteMember(id: Long) {
        val member = getMemberById(id)
        member.deletedDate = LocalDateTime.now()
    }

    fun getMembers(page: Int, size: Int): Page<AdminMemberDto> {
        return memberRepository.findAll(
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
            )
        ).map { m -> AdminMemberDto(m) }
    }


    fun genAccessToken(member: Member): String =
        authTokenService.genAccessToken(member)

    fun payload(accessToken: String): Map<String, Any>? =
        authTokenService.payload(accessToken)

    fun count(): Long = memberRepository.count()

    fun checkPassword(member: Member, password: String) {
        if (!passwordEncoder.matches(password, member.password))
            throw ServiceException("401-1", "비밀번호가 일치하지 않습니다.")
    }

    fun getMemberByGithubId(githubId: String): Member =
        memberRepository.findByGithubId(githubId)
            ?: throw NoSuchElementException("존재하지 않는 회원입니다.")

    @Transactional
    fun modifyOrJoin(username: String, password: String, nickname: String, profileImgUrl: String?): Member {
        val member = memberRepository.findByUsername(username)
        if (member == null) {
            return join(username, password, nickname, profileImgUrl)
        }

        if (member.deletedDate != null) {
            // 재가입 시나리오 실행
            member.reSignup(encodePasswordImplementation(password), nickname, profileImgUrl)
            return member
        }

        modify(member, nickname, profileImgUrl)

        return member
    }

    private fun modify(member: Member, nickname: String, profileImgUrl: String?) {
        member.modify(nickname, profileImgUrl)
    }

    private fun encodePasswordImplementation(password: String): String =
        requireNotNull(passwordEncoder.encode(password))
}