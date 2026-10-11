package com.back.domain.member.entity

import com.back.domain.book.entity.Book
import com.back.domain.wish.entity.Wish
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Member(
    id: Long,

    @field:Column(unique = true) val username: String? = null,
    var password: String? = null,
    var nickname: String,

    @field:Column(unique = true)
    val githubId: String? = null,
    var imgUrl: String? = null,

    @field:Column(unique = true)
    var refreshToken: String? = null,
) : BaseEntity(id) {

    @Column(unique = true)
    var githubLink: String? = null

    @Column(unique = true)
    var widgetLink: String? = null

    var deletedDate: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    val wishes: MutableList<Wish> = mutableListOf()

    constructor(id: Long, username: String, name: String, role: Role) : this(id, username = username, nickname = name) {
        this.role = role
    }

    constructor(username: String, password: String, githubId: String?, nickname: String, imgUrl: String?) : this(
        0,
        username = username,
        password = password,
        nickname = nickname,
        githubId = githubId,
        imgUrl = imgUrl,
        refreshToken = UUID.randomUUID().toString()
    )

    val name: String?
        get() = nickname

    val isAdmin: Boolean
        get() = role == Role.ADMIN

    val isDeleted: Boolean
        get() = deletedDate != null

    private val authoritiesAsStringList: List<String>
        get() = listOf("ROLE_" + role.name)

    val authorities: Collection<GrantedAuthority>
        get() = authoritiesAsStringList.map { SimpleGrantedAuthority(it) }

    fun setName(name: String) {
        this.nickname = name
    }

    fun modifyRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    fun grantAdmin() {
        role = Role.ADMIN
    }

    fun reSignup(password: String, nickname: String, profileImgUrl: String?) {
        this.password = password
        refreshToken = UUID.randomUUID().toString()
        deletedDate = null
        modify(nickname, profileImgUrl)
    }

    fun addWish(book: Book) {
        wishes.add(Wish(this, book))
    }

    fun deleteWish(book: Book) {
        wishes.removeAll { it.book == book }
    }

    fun modify(nickname: String, profileImgUrl: String?) {
        this.nickname = nickname
        this.imgUrl = profileImgUrl
    }
}