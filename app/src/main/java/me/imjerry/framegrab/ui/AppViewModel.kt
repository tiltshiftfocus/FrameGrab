package me.imjerry.framegrab.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel: ViewModel() {
    private val _appIsLoading = MutableStateFlow(false)
    val appIsLoading: StateFlow<Boolean> = _appIsLoading.asStateFlow()

    fun setIsLoading(state: Boolean) {
        _appIsLoading.update { state }
    }
}