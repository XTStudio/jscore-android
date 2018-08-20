package com.xt.jscore

import android.os.Handler
import android.os.Looper
import com.eclipsesource.v8.Releasable
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/7/19.
 */
class JSContext: MutableMap<String, Any> {

    val runtime = V8.createV8Runtime("global")

    val globalObject: JSValue

    var exceptionHandler: ((context: JSContext, exception: Exception) -> Unit)? = null

    var name: String = "No Name"

    val handler: Handler

    internal val releasables: MutableSet<Releasable>

    fun finalize() {
        if (this.runtime.isReleased) { return }
        handler.post {
            try {
                releasables.forEach { it.release() }
                runtime.release(false)
            } catch (e: Exception) {}
        }
    }

    fun destory() {
        if (this.runtime.isReleased) { return }
        handler.post {
            try {
                releasables.forEach { it.release() }
                runtime.release(false)
            } catch (e: Exception) {}
        }
    }

    init {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
        this.handler = Handler()
        this.releasables = mutableSetOf()
        this.globalObject = JSValue(runtime.getObject("global"), this)
    }

    fun evaluateScript(script: String): JSValue? {
        if (this.checkReleased()) { return null }
        JSContext.currentContext = this
        return try {
            val returnValue = this.runtime.executeScript(script)
            JSValue(returnValue, this)
        } catch (e: Exception) {
            this.exceptionHandler?.invoke(this, e)
            JSValue(V8.getUndefined(), this)
        } finally {
            JSContext.currentContext = null
        }
    }

    private fun checkReleased(): Boolean {
        return this.runtime.isReleased
    }

    // SubscriptSupport

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() {
            return mutableSetOf()
        }

    override val keys: MutableSet<String>
        get() {
            if (this.checkReleased()) { return mutableSetOf() }
            return runtime.keys.toMutableSet()
        }

    override val size: Int
        get() {
            if (this.checkReleased()) { return 0 }
            return runtime.keys.count()
        }

    override val values: MutableCollection<Any>
        get() {
            return mutableListOf()
        }

    override fun containsKey(key: String): Boolean {
        if (this.checkReleased()) { return false }
        return runtime.contains(key)
    }

    override fun containsValue(value: Any): Boolean {
        return false
    }

    override fun get(key: String): JSValue? {
        if (this.checkReleased()) { return null }
        return if (runtime.contains(key)) JSValue(runtime.get(key), this) else null
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun clear() { }

    override fun put(key: String, value: Any): Any? {
        if (this.checkReleased()) { return value }
        val v8Value = (value as? JSValue)?.v8Value ?: value
        (v8Value as? Int)?.let { this.runtime.add(key, it) }
        (v8Value as? Float)?.let { this.runtime.add(key, it.toDouble()) }
        (v8Value as? Double)?.let { this.runtime.add(key, it) }
        (v8Value as? String)?.let { this.runtime.add(key, it) }
        (v8Value as? V8Value)?.let { this.runtime.add(key, it) }
        (v8Value as? JSExport)?.let {
            val v8Object = JSExportCreateV8Object(it, this)
            this.runtime.add(key, v8Object)
            v8Object.release()
        }
        return value
    }

    override fun putAll(from: Map<out String, Any>) {
        from.forEach { this.put(it.key, it.value) }
    }

    override fun remove(key: String): Any? {
        if (this.checkReleased()) { return null }
        val value = runtime.get(key)
        try {
            runtime.executeScript("$key = undefined")
        } catch (e: Exception) { }
        return JSValue(value, this)
    }

    companion object {

        var currentContext: JSContext? = null
            internal set

        fun setCurrentContext(context: JSContext?) {
            this.currentContext = context
        }

    }

}