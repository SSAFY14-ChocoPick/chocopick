package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.source.local.SelectedStoreLocalDataSource

class SelectedStoreRepository(
    private val local : SelectedStoreLocalDataSource
) {

    fun getSelectedStore() : Store? = local.load()

    fun selectStore(store : Store) {
        local.save(store)
    }

    fun clear(){
        local.clear()
    }
}