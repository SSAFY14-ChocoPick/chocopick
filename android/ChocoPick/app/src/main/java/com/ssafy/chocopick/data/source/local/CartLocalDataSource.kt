package com.ssafy.chocopick.data.source.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.chocopick.data.model.CartItem

class CartLocalDataSource(
    context : Context,
    private val gson : Gson,
    private val uid : String
) {
    private val prefs = context.getSharedPreferences("chocopick_cart_$uid", Context.MODE_PRIVATE)
    private val KEY_CART = "cart_item_json"

    fun load() : List<CartItem> {
        val json = prefs.getString(KEY_CART,null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return runCatching { gson.fromJson<List<CartItem>>(json,type) }
            .getOrElse { emptyList() }
    }

    fun save(items : List<CartItem>){
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_CART,json).apply()
    }

    fun clear(){
        prefs.edit().remove(KEY_CART).apply()
    }
}