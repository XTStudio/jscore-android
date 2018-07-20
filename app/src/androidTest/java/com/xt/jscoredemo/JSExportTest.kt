package com.xt.jscoredemo

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.xt.jscore.JSContext
import com.xt.jscore.JSExport
import com.xt.jscore.JSValue
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs


interface FooClassExport: JSExport {

    fun exportMethod()

    fun exportMethodWithArguments(intValue: Int, doubleValue: Double, floatValue: Float)

    fun exportMethodWithJSValueArguments(value: Map<String, Any>?)

}

open class FooClass: FooClassExport {

    var methodCalled = false
    var methodWithArgumentsCalled = false
    var methodWithJSValueArguments = false

    override fun exportMethod() {
        methodCalled = true
    }

    override fun exportMethodWithArguments(intValue: Int, doubleValue: Double, floatValue: Float) {
        if (intValue == 1 && abs(doubleValue - 2.0) < 0.01 && floatValue == 3.0f) {
            methodWithArgumentsCalled = true
        }
    }

    override fun exportMethodWithJSValueArguments(value: Map<String, Any>?) {
        if (value!!["aKey"] == "aValue") {
            methodWithJSValueArguments = true
        }
    }

}

class BarClass: FooClass() {

}

/**
 * Created by cuiminghui on 2018/7/20.
 */
@RunWith(AndroidJUnit4::class)
class JSExportTest {

    val context = JSContext()

    init {
        context.exceptionHandler = { _, exception ->
            Log.e("JSContext", exception.toString())
        }
    }

    @Test
    fun testExports() {
        val bar = BarClass()
        context["foo"] = bar
        context.evaluateScript("foo.exportMethod()")
        assertTrue(bar.methodCalled)
        context.evaluateScript("foo.exportMethodWithArguments(1, 2.0, 3.0)")
        assertTrue(bar.methodWithArgumentsCalled)
        context.evaluateScript("foo.exportMethodWithJSValueArguments({aKey: 'aValue'})")
        assertTrue(bar.methodWithJSValueArguments)
    }

}