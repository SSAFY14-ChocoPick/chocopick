package com.ssafy.chocopick.ui.order

import android.content.Context
import com.google.gson.Gson
import com.ssafy.chocopick.data.repository.CartRepository
import com.ssafy.chocopick.data.repository.CartRepositoryImpl
import com.ssafy.chocopick.data.source.local.CartLocalDataSource

object ServiceLocator {

    @Volatile private var cartRepo: CartRepository? = null
    private val gson: Gson by lazy { Gson() }

    fun provideCartRepository(appContext: Context, uid : String): CartRepository {
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

    fun clearCartRepository(){
        cartRepo = null
    }
}