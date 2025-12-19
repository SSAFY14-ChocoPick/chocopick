package com.ssafy.chocopick.data.model

data class CartItem (
    val productId: String = "",
    val name: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val updatedAt: Long = 0L
){

}