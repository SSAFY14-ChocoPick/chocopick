package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.Product

//object CartRepository {
//
//    private val cartItems = mutableMapOf<String, CartItem>()
//
//    fun add(product : Product, qty : Int){
//        val existing = cartItems[product.productId]
//
//        if(existing!=null){
//            cartItems[product.productId] = existing.copy(quantity = existing.quantity + qty, updatedAt = System.currentTimeMillis())
//        }else{
//            cartItems[product.productId] =
//                CartItem(
//                    productId = product.productId,
//                    name = product.name,
//                    price = product.price,
//                    imageUrl = product.imageUrl,
//                    quantity = qty,
//                    updatedAt = System.currentTimeMillis()
//                )
//        }
//    }
//
//    fun getItems(): List<CartItem> = cartItems.values.toList()
//
//    fun remove(productId : String){
//        cartItems.remove(productId)
//    }
//
//    fun clear() {
//        cartItems.clear()
//    }
//
//    fun increase(productId : String){
//        val item = cartItems[productId] ?: return
//        cartItems[productId] = item.copy(
//            quantity = item.quantity+1,
//            updatedAt = System.currentTimeMillis()
//        )
//
//    }
//
//    fun decrease(productId : String){
//        val item = cartItems[productId] ?: return
//        if(item.quantity<=1) return
//        cartItems[productId] = item.copy(
//            quantity = item.quantity-1,
//            updatedAt = System.currentTimeMillis()
//        )
//    }
//
//}


interface CartRepository {
    fun getItems(): List<CartItem>
    fun add(product: Product, qty: Int)
    fun increase(productId: String)
    fun decrease(productId: String)
    fun remove(productId: String)
    fun clear()
}