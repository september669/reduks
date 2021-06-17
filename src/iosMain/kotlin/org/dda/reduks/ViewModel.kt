package org.dda.reduks
import kotlin.native.concurrent.ThreadLocal
import kotlin.native.internal.GC


@ThreadLocal
private var isGCWorking = false

actual  open class ViewModel {

    actual open fun onCleared(){
        // run Kotlin/Native GC
        if (!isGCWorking) {
            isGCWorking = true
            GC.collect()
            isGCWorking = false
        }
    }
}