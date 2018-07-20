package com.xt.jscoredemo

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.xt.jscore.JSContext
import com.xt.jscore.JSValue
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by cuiminghui on 2018/7/20.
 */

@RunWith(AndroidJUnit4::class)
class JSValueTest {

    val context = JSContext()

    init {
        context.exceptionHandler = { _, exception ->
            Log.e("JSContext", exception.toString())
        }
    }

    @Test
    fun testUndefinedValue() {
        val value = context.evaluateScript("undefined") as JSValue
        assertNotNull(value)
        assertTrue(value.isUndefined)
    }

    @Test
    fun testNullValue() {
        val value = context.evaluateScript("null") as JSValue
        assertNotNull(value)
        assertTrue(value.isNull)
    }

    @Test
    fun testBoolValue() {
        val value = context.evaluateScript("true") as JSValue
        assertNotNull(value)
        assertTrue(value.isBoolean)
        assertEquals(value.toBool(), true)
    }

    @Test
    fun testNumberValue() {
        val value = context.evaluateScript("1 + 1") as JSValue
        assertNotNull(value)
        assertTrue(value.isNumber)
        assertEquals(value.toInt(), 2)
        assertEquals(value.toDouble(), 2.0, 0.01)
        val nanValue = context.evaluateScript("'abcd'") as JSValue
        assertFalse(nanValue.isNumber)
        assertNull(nanValue.toNumber())
    }

    @Test
    fun testStringValue() {
        val value = context.evaluateScript("'Hello, World!'") as JSValue
        assertNotNull(value)
        assertTrue(value.isString)
        assertEquals(value.toString(), "Hello, World!")
    }

    @Test
    fun testObjectValue() {
        val value = context.evaluateScript("var a = {aKey: 'aValue'}; a") as JSValue
        assertNotNull(value)
        assertTrue(value.isObject)
        assertEquals(value.toDictionary()!!["aKey"], "aValue")
    }

    @Test
    fun testArrayValue() {
        val value = context.evaluateScript("[1, 2, 3, 4]") as JSValue
        assertNotNull(value)
        assertTrue(value.isArray)
        assertEquals(value.toList()!![0], 1)
        assertEquals(value.toList()!![3], 4)
    }

    @Test
    fun testSubscript() {
        val value = JSValue.valueWithNewObjectInContext(context)
        value["foo"] = "Hello, World!"
        assertEquals((value["foo"] as JSValue).toString(), "Hello, World!")
    }

}