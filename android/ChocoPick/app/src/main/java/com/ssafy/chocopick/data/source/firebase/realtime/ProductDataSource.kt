package com.ssafy.chocopick.data.source.firebase.realtime

import com.google.firebase.database.FirebaseDatabase
import com.ssafy.chocopick.data.model.Product
import kotlinx.coroutines.tasks.await

class ProductDataSource(
    private val db : FirebaseDatabase = FirebaseDatabase.getInstance("https://chocopick-b0c91-default-rtdb.asia-southeast1.firebasedatabase.app/")
) {
    private val ref = db.getReference("products")

    suspend fun fetchProduct() : List<Product>{
        val snapshot = ref.get().await()
        return snapshot.children.mapNotNull {
            child ->
            val productId = child.key ?: return@mapNotNull null
            val p = child.getValue(Product::class.java) ?: return@mapNotNull null
            p.copy(productId = productId)
        }
    }

    suspend fun fetchProductById(productId : String) : Product {
        val snapshot = ref.child(productId).get().await()
        val p = snapshot.getValue(Product::class.java)
        return p!!.copy(productId = productId)
    }
}