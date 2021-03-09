package org.dda.reduks.coroutines

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

suspend fun <T> MutableSharedFlow<T>.whenSubscribed(block: suspend MutableSharedFlow<T>.() -> Unit) {
    subscriptionCount.first { activeCount -> activeCount > 0 }
    block(this)
}
