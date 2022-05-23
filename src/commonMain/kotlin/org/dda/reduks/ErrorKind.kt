package org.dda.reduks

sealed class ErrorKind(open val exception: Throwable?) {
    data class None(override val exception: Throwable? = null) : ErrorKind(exception)
    data class Generic(override val exception: Throwable? = null) : ErrorKind(exception)
    data class Network(override val exception: Throwable? = null) : ErrorKind(exception)
    data class Maintenance(override val exception: Throwable? = null) : ErrorKind(exception)
    data class NotFound(override val exception: Throwable? = null) : ErrorKind(exception)
    data class Api(
        override val exception: Throwable? = null,
        val httpStatusCode: Int,
        val apiErrorCode: Int
    ) : ErrorKind(exception)
}
