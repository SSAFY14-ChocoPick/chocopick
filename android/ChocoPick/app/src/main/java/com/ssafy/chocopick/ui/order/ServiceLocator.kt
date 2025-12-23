package com.ssafy.chocopick.ui.order

import android.content.Context
import com.google.gson.Gson
import com.ssafy.chocopick.data.repository.*
import com.ssafy.chocopick.data.source.firebase.realtime.OrderDataSource
import com.ssafy.chocopick.data.source.firebase.realtime.StoreDataSource
import com.ssafy.chocopick.data.source.local.CartLocalDataSource

object ServiceLocator {

    @Volatile private var cartRepo: CartRepository? = null
    @Volatile private var orderRepo: OrderRepository? = null
    @Volatile private var storeRepo: StoreRepository? = null

    private val gson: Gson by lazy { Gson() }

    // =======================
    // CartRepository
    // =======================
    fun provideCartRepository(appContext: Context, uid: String): CartRepository {
        return cartRepo ?: synchronized(this) {
            cartRepo ?: CartRepositoryImpl(
                local = CartLocalDataSource(
                    context = appContext.applicationContext,
                    gson = gson,
                    uid = uid
                )
            ).also { cartRepo = it }
        }
    }

    fun clearCartRepository() {
        cartRepo = null
    }

    // =======================
    // StoreRepository (OrderRepository에서 필요)
    // =======================
    fun provideStoreRepository(): StoreRepository {
        return storeRepo ?: synchronized(this) {
            storeRepo ?: StoreRepositoryImpl(
                storeDataSource  = StoreDataSource()
            ).also { storeRepo = it }
        }
    }


    // =======================
    // OrderRepository
    // =======================
    fun provideOrderRepository(appContext: Context): OrderRepository {
        return orderRepo ?: synchronized(this) {
            orderRepo ?: OrderRepositoryImpl(
                orderDataSource = OrderDataSource(),
                storeRepository = provideStoreRepository()
            ).also { orderRepo = it }
        }
    }


    fun clearOrderRepository() {
        orderRepo = null
    }
}
