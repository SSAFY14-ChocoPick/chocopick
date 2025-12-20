package com.ssafy.chocopick.ui.order

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CartViewModelFactory(private val app : Application,private val uid : String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = ServiceLocator.provideCartRepository(app.applicationContext,uid)
        return CartViewModel(repo) as T    }
}