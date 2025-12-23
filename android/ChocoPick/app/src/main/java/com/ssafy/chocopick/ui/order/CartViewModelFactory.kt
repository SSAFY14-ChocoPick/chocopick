package com.ssafy.chocopick.ui.order

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CartViewModelFactory(
    private val appContext: Context,
    private val uid: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val cartRepository = ServiceLocator
            .provideCartRepository(appContext, uid)

        @Suppress("UNCHECKED_CAST")
        return CartViewModel(cartRepository) as T
    }
}
