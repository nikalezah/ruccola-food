package kz.ruccola.food

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class SessionViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun clear() {
        viewModelStore.clear()
    }
}
