package org.dda.reduks.coroutines

import kotlinx.coroutines.CoroutineDispatcher


interface CoroutineDispatchers {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
    val io: CoroutineDispatcher
}