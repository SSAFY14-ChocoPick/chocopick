package com.ssafy.chocopick.data.source.beacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.BeaconRepository

class BeaconViewModelFactory(
    private val repo: BeaconRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeaconViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeaconViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}