package org.dda.ankoLogger

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTags {

    class TestClass : AnkoLogger

    private var printer = TestLogFuncs.TestPrinter()

    @BeforeTest
    fun setup() {
        printer = TestLogFuncs.TestPrinter()
        configAnkoLogger(
            applicationTag = "AppTag",
            listPrinters = listOf(printer)
        )
    }

    @Test
    fun checkAppTag() {
        TestClass().logDebug { "any" }
        assertEquals(expected = "AppTag", printer.lastEntry.appTag)
    }

    @Test
    fun checkClassTag() {
        val foo = TestClass()
        assertTrue { "TestClass.+".toRegex().matches(foo.loggerTag) }
    }

    @Test
    fun checkLocalLogger() {
        val logTag = "TestLoggerName"
        val foo = ankoLogger(logTag)
        assertEquals(expected = logTag, actual = foo.loggerTag)
    }
}