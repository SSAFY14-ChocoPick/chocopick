package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.source.local.CartLocalDataSource

class CartRepositoryImpl (
    private val local: CartLocalDataSource
) : CartRepository{
    // 앱 시작 시 로컬에서 읽어와서 메모리에 들고 있음
    private val items = local.load().toMutableList()

    private fun persist() {
        local.save(items)
    }

    override fun getItems(): List<CartItem> = items.toList()

    override fun add(product: Product, qty: Int) {
        if (qty <= 0) return
        val idx = items.indexOfFirst { it.productId == product.productId }
        if (idx >= 0) {
            val cur = items[idx]
            items[idx] = cur.copy(
                quantity = cur.quantity + qty,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            items.add(
                CartItem(
                    productId = product.productId,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    quantity = qty,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        persist()
    }

    override fun increase(productId: String) {
        val idx = items.indexOfFirst { it.productId == productId }
        if (idx < 0) return
        val cur = items[idx]
        items[idx] = cur.copy(quantity = cur.quantity + 1, updatedAt = System.currentTimeMillis())
        persist()
    }

    override fun decrease(productId: String) {
        val idx = items.indexOfFirst { it.productId == productId }
        if (idx < 0) return
        val cur = items[idx]
        val next = cur.quantity - 1
        if (next <= 0) {
            items.removeAt(idx)
        } else {
            items[idx] = cur.copy(quantity = next, updatedAt = System.currentTimeMillis())
        }
        persist()
    }

    override fun remove(productId: String) {
        items.removeAll { it.productId == productId }
        persist()
    }

    override fun clear() {
        items.clear()
        local.clear()
    }

}