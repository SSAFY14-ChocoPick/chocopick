package com.ssafy.chocopick.data.source.local

import android.content.Context
import com.google.gson.Gson
import com.ssafy.chocopick.data.model.Store

//사용자가 저장한 본인이 선택한 매장.
class SelectedStoreLocalDataSource(
    context : Context,
    private val gson : Gson = Gson(),
    uid : String
) {
    private val prefs = context.getSharedPreferences("selected_store_prefs_$uid", Context.MODE_PRIVATE)
    private val KEY_STORE = "selected_store"

    fun load() : Store? {
        val json = prefs.getString(KEY_STORE,null) ?: return null
        return runCatching {
            gson.fromJson(json, Store::class.java)
        }.getOrNull()
    }

    fun save(store : Store){
        prefs.edit().putString(KEY_STORE,gson.toJson(store))
            .apply()
    }

    fun clear(){
        prefs.edit().remove(KEY_STORE).apply()
    }

}