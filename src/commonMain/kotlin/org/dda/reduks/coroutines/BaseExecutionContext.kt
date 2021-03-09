package org.dda.reduks.coroutines

import org.dda.ankoLogger.AnkoLogger
import org.dda.ankoLogger.logDebug


interface BaseExecutionContext : AnkoLogger {

    var isDestroyedContext: Boolean

    fun handleException(exc: Throwable): Boolean {// returns true if exception absorbed
        return false
    }

    fun showProgress(show: Boolean, progress: ExecutionProgress)

    fun isActive() = !isDestroyedContext

    fun checkDestroyed() {
        if (isDestroyedContext) {
            cleanExecutionContext()
        }
    }

    fun cleanExecutionContext() {
        logDebug { "BaseExecutionContext.cleanExecutionContext()" }
        isDestroyedContext = true
    }

}