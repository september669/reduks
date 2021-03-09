package org.dda.ankoLogger

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestLogFuncs {

    private var printer = TestPrinter()

    @BeforeTest
    fun setup() {
        printer = TestPrinter()
        configAnkoLogger(
            applicationTag = "AppTag",
            listPrinters = listOf(printer)
        )
    }

    private fun checkLogEntry(
        expectMessage: String,
        expectException: Throwable?,
        expectLogLevel: LogLevel
    ) {
        assertTrue { printer.list.size == 1 }
        assertEquals(expected = expectMessage, actual = printer.lastEntry.msg)
        assertEquals(expected = expectException, actual = printer.lastEntry.thr)
        assertEquals(expected = expectLogLevel, actual = printer.lastEntry.level)
    }

    /////////////////////////////////////////

    @Test
    fun logVerbose() {
        val message = "logVerbose message"
        val exception = IndexOutOfBoundsException()
        TestClass().logVerbose(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Verbose
        )
    }

    @Test
    fun logVerboseLambda() {
        val message = "logVerboseLambda message"
        TestClass().logVerbose { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Verbose
        )
    }

    //

    @Test
    fun logDebug() {
        val message = "logDebug message"
        val exception = IndexOutOfBoundsException()
        TestClass().logDebug(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Debug
        )
    }

    @Test
    fun logDebugLambda() {
        val message = "logDebugLambda message"
        TestClass().logDebug { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Debug
        )
    }

    //

    @Test
    fun logInfo() {
        val message = "logInfo message"
        val exception = IndexOutOfBoundsException()
        TestClass().logInfo(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Info
        )
    }

    @Test
    fun logInfoLambda() {
        val message = "logInfoLambda message"
        TestClass().logInfo { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Info
        )
    }

    //

    @Test
    fun logWarn() {
        val message = "logWarn message"
        val exception = IndexOutOfBoundsException()
        TestClass().logWarn(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Warn
        )
    }

    @Test
    fun logWarnLambda() {
        val message = "logWarnLambda message"
        TestClass().logWarn { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Warn
        )
    }

    //

    @Test
    fun logError() {
        val message = "logError message"
        val exception = IndexOutOfBoundsException()
        TestClass().logError(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Error
        )
    }

    @Test
    fun logErrorLambda() {
        val message = "logErrorLambda message"
        TestClass().logError { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Error
        )
    }

    //

    @Test
    fun logAssert() {
        val message = "logAssert message"
        val exception = IndexOutOfBoundsException()
        TestClass().logAssert(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Assert
        )
    }

    @Test
    fun logAssertLambda() {
        val message = "logAssertLambda message"
        TestClass().logAssert { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Assert
        )
    }

    //

    @Test
    fun logWtf() {
        val message = "logWtf message"
        val exception = IndexOutOfBoundsException()
        TestClass().logWtf(message, exception)
        checkLogEntry(
            expectMessage = message,
            expectException = exception,
            expectLogLevel = LogLevel.Wtf
        )
    }

    @Test
    fun logWtfLambda() {
        val message = "logWtfLambda message"
        TestClass().logWtf { message }
        checkLogEntry(
            expectMessage = message,
            expectException = null,
            expectLogLevel = LogLevel.Wtf
        )
    }

    /////////////////////////////////////////

    class TestClass : AnkoLogger

    class TestPrinter : LogPrinter {

        private val listAtomicRef = AtomicRef(emptyList<LogEntry>().freeze())

        val list: List<LogEntry> get() = listAtomicRef.value

        val lastEntry: LogEntry get() = list.last()

        override fun log(
            appTag: String,
            tag: String,
            level: LogLevel,
            msg: String,
            thr: Throwable?
        ) {
            listAtomicRef.value = (
                    listAtomicRef.value + listOf(
                        LogEntry(
                            appTag = appTag,
                            tag = tag,
                            level = level,
                            msg = msg,
                            thr = thr
                        )
                    )
                    ).freeze()
        }

    }

    data class LogEntry(
        val appTag: String,
        val tag: String,
        val level: LogLevel,
        val msg: String,
        val thr: Throwable?,
    )
}