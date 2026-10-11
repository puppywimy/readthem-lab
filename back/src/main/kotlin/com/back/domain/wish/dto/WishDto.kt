package com.back.domain.wish.dto

import com.back.domain.wish.entity.Wish

class WishDto(val id: Long) {
    constructor(wish: Wish) : this(wish.id)
}