package org.dda.reduks

sealed class ErrorKind(open val exception: Exception?) {
    data class None(override val exception: Exception? = null) : ErrorKind(exception)
    data class Generic(override val exception: Exception? = null) : ErrorKind(exception)
    data class Network(override val exception: Exception? = null) : ErrorKind(exception)
    data class Maintenance(override val exception: Exception? = null) : ErrorKind(exception)
    data class NotFound(override val exception: Exception? = null) : ErrorKind(exception)
    data class Api(
        override val exception: Exception? = null,
        val httpStatusCode: Int,
        val apiErrorCode: Int
    ) : ErrorKind(exception)
}
