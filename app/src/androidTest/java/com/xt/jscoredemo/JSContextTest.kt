package com.xt.jscoredemo

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class JSContextTest {

    val context = JSContext()

    init {
        context.exceptionHandler = { _, exception ->
            Log.e("JSContext", exception.toString())
        }
    }

    @Test
    fun evaluateNormalScript() {
        val value = context.evaluateScript("1 + 1") as JSValue
        assertNotNull(value)
        assertTrue(value.isNumber)
        assertEquals(value.toInt(), 2)
    }

    @Test
    fun evaluateIssueScript() {
        val value = context.evaluateScript("a + b") as JSValue
        assertTrue(value.isUndefined)
    }

    @Test
    fun testSubscript() {
        context["foo"] = "Hello, World!"
        val fooValue = context["foo"]
        assertEquals(fooValue.toString(), "Hello, World!")
    }

    @Test
    fun testGlobalObject() {
        context.evaluateScript("global.testGlobalObject = 1;")
        val fooValue = context["testGlobalObject"]
        assertEquals(fooValue.toInt(), 1)
    }

}
