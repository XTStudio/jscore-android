package com.xt.jscore

import android.os.Handler
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/7/19.
 */
class JSContext: MutableMap<String, JSValue> {

    val runtime = V8.createV8Runtime("global")

    val globalObject: JSValue = JSValue(runtime.getObject("global"))

    var exceptionHandler: ((context: JSContext, exception: Exception) -> Unit)? = null

    var name: String = "No Name"

    private val handler = Handler()

    fun finalize() {
        runtime.release(false)
    }

    fun evaluateScript(script: String): JSValue? {
        return try {
            val returnValue = this.runtime.executeObjectScript(script)
            JSValue(returnValue)
        } catch (e: Exception) {
            JSValue(V8.getUndefined())
        }
    }

    // SubscriptSupport

    override val entries: MutableSet<MutableMap.MutableEntry<String, JSValue>>
        get() {
            return mutableSetOf()
        }

    override val keys: MutableSet<String>
        get() {
            return runtime.keys.toMutableSet()
        }

    override val size: Int
        get() {
            return runtime.keys.count()
        }

    override val values: MutableCollection<JSValue>
        get() {
            return mutableListOf()
        }

    override fun containsKey(key: String): Boolean {
        return runtime.contains(key)
    }

    override fun containsValue(value: JSValue): Boolean {
        return false
    }

    override fun get(key: String): JSValue? {
        return if (runtime.contains(key)) JSValue(runtime.get(key)) else null
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun clear() { }

    override fun put(key: String, value: JSValue): JSValue? {
        (value.managedValue as? Int)?.let { this.runtime.add(key, it) }
        (value.managedValue as? Float)?.let { this.runtime.add(key, it.toDouble()) }
        (value.managedValue as? Double)?.let { this.runtime.add(key, it) }
        (value.managedValue as? String)?.let { this.runtime.add(key, it) }
        (value.managedValue as? V8Value)?.let { this.runtime.add(key, it) }
        return value
    }

    override fun putAll(from: Map<out String, JSValue>) {
        from.forEach { this.put(it.key, it.value) }
    }

    override fun remove(key: String): JSValue? {
        val value = runtime.get(key)
        try {
            runtime.executeScript("$key = undefined")
        } catch (e: Exception) { }
        return JSValue(value)
    }

    companion object {

        var currentContext: JSContext? = null
            private set

    }

}