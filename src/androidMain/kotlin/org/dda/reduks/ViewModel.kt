package org.dda.reduks

import androidx.lifecycle.ViewModel

actual open class ViewModel : ViewModel() {

    public actual override fun onCleared() {
        super.onCleared()
    }
}