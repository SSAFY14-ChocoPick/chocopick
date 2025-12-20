package com.ssafy.chocopick.ui.home.store

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ssafy.chocopick.data.repository.SelectedStoreRepository
import com.ssafy.chocopick.data.source.local.SelectedStoreLocalDataSource

class SelectedStoreViewModelFactory(
    private val app : Application,
    private val gson : Gson = Gson(),
    private val uid : String

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectedStoreViewModel::class.java)) {
            val local = SelectedStoreLocalDataSource(app.applicationContext, gson, uid)
            val repo = SelectedStoreRepository(local)
            return SelectedStoreViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}