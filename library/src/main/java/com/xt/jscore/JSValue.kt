package com.xt.jscore

import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.V8ObjectUtils
import java.lang.ref.WeakReference

/**
 * Created by cuiminghui on 2018/7/19.
 */
class JSValue(val v8Value: Any?, context: JSContext): MutableMap<String, Any> {

    val context: WeakReference<JSContext> = WeakReference(context)

    val isUndefined: Boolean = v8Value == null || (v8Value as? V8Value)?.v8Type == 99

    val isNull: Boolean = v8Value == null || (v8Value as? V8Value)?.v8Type == 0

    val isBoolean: Boolean = v8Value is Boolean || (v8Value as? V8Value)?.v8Type == 3

    val isNumber: Boolean = v8Value is Number || (v8Value as? V8Value)?.v8Type == 1 || (v8Value as? V8Value)?.v8Type == 2

    val isString: Boolean = v8Value is String || (v8Value as? V8Value)?.v8Type == 4

    val isObject: Boolean = (v8Value as? V8Value)?.v8Type == 6

    val isArray: Boolean = (v8Value as? V8Value)?.v8Type == 5

    fun finalize() {
        if (v8Value !is Releasable) { return }
        val v8Value = (v8Value as? V8Value) ?: return
        this.context.get()?.handler?.post {
            if (!v8Value.runtime.isReleased && !v8Value.isReleased) {
                v8Value.release()
            }
        }
    }

    init {
        (v8Value as? Releasable)?.let { context.releasables.add(it) }
    }

    fun toBool(): Boolean { return v8Value as? Boolean ?: false }

    fun toDouble(): Double { return (v8Value as? Number)?.toDouble() ?: 0.0 }

    fun toInt(): Int { return (v8Value as? Number)?.toInt() ?: 0 }

    fun toNumber(): Number? { return v8Value as? Number }

    override fun toString(): String {
        return if (this.isString) {
            v8Value as? String ?: ""
        }
        else {
            if (checkReleased()) { return "" }
            (v8Value as? V8Value)?.toString() ?: ""
        }
    }

    fun toArray(): List<Any?>? {
        if (checkReleased()) { return null }
        if (this.isArray) {
            return V8ObjectUtils.toList((v8Value as? V8Array)).toList()
        }
        return null
    }

    fun toList() = toArray()

    fun toDictionary(): Map<String, Any?>? {
        if (checkReleased()) { return null }
        if (this.isObject) {
            return V8ObjectUtils.toMap(v8Value as? V8Object).toMap()
        }
        return null
    }

    fun toMap() = toDictionary()

    fun valueForProperty(property: String): JSValue? {
        if (checkReleased()) { return null }
        val context = this.context.get() ?: return null
        if (this.isObject) {
            return JSValue((v8Value as? V8Object)?.get(property), context)
        }
        return null
    }

    fun setValueForProperty(value: Any, forProperty: String) {
        if (checkReleased()) { return  }
        if (this.isObject) {
            val v8Object = (v8Value as? V8Object) ?: return
            val v8Value = (value as? JSValue)?.v8Value ?: value
            (v8Value as? Int)?.let { v8Object.add(forProperty, it) }
            (v8Value as? Float)?.let { v8Object.add(forProperty, it.toDouble()) }
            (v8Value as? Double)?.let { v8Object.add(forProperty, it) }
            (v8Value as? String)?.let { v8Object.add(forProperty, it) }
            (v8Value as? V8Value)?.let { v8Object.add(forProperty, it) }
            (v8Value as? JSExport)?.let {
                val context = this.context.get() ?: return@let
                val v8Object = JSExportCreateV8Object(it, context)
                context.runtime.add(forProperty, v8Object)
                v8Object.release()
            }
        }
    }

    fun valueAtIndex(index: Int): JSValue? {
        if (checkReleased()) { return null }
        val context = this.context.get() ?: return null
        if (this.isArray) {
            val v8Array = (v8Value as? V8Array) ?: return null
            return JSValue(v8Array.get(index), context)
        }
        return null
    }

    fun setValueAtIndex(value: Any, index: Int) {
        if (checkReleased()) { return  }
        if (this.isArray) {
            val v8Array = (v8Value as? V8Array) ?: return
            val v8Value = (value as? JSValue)?.v8Value ?: value
            (v8Value as? Int)?.let { v8Array.add(index.toString(), it) }
            (v8Value as? Float)?.let { v8Array.add(index.toString(), it.toDouble()) }
            (v8Value as? Double)?.let { v8Array.add(index.toString(), it) }
            (v8Value as? String)?.let { v8Array.add(index.toString(), it) }
            (v8Value as? V8Value)?.let { v8Array.add(index.toString(), it) }
        }
    }

    fun callWithArguments(arguments: List<Any>): JSValue? {
        if (checkReleased()) { return null }
        val context = this.context.get() ?: return null
        JSContext.currentContext = context
        return try {
            val v8Function = this.v8Value as? V8Function ?: return null
            val v8Arguments = V8ObjectUtils.toV8Array(v8Function.runtime, arguments)
            val result = v8Function.call(null, v8Arguments)
            v8Arguments.release()
            JSValue(result, context)
        } catch (e: Exception) {
            null
        } finally {
            JSContext.currentContext = null
        }
    }

    fun invokeMethod(method: String, arguments: List<Any>): JSValue? {
        if (checkReleased()) { return null }
        val context = this.context.get() ?: return null
        JSContext.currentContext = context
        return try {
            val v8Object = this.v8Value as? V8Object ?: return null
            val v8Arguments = V8ObjectUtils.toV8Array(v8Object.runtime, arguments)
            val result = v8Object.executeJSFunction(method, v8Arguments)
            v8Arguments.release()
            return JSValue(result, context)
        }
        catch (e: Exception) {
            null
        }
        finally {
            JSContext.currentContext = null
        }
    }

    private fun checkReleased(): Boolean {
        (v8Value as? V8Value)?.takeIf { v8Value is Releasable }?.let {
            return it.isReleased || it.runtime.isReleased
        }
        return false
    }

    // SubscriptSupport

    override val size: Int
        get() {
            return 0
        }

    override fun containsKey(key: String): Boolean {
        if (this.checkReleased()) { return false }
        return (this.v8Value as? V8Object)?.contains(key) ?: false
    }

    override fun containsValue(value: Any): Boolean {
        return false
    }

    override fun get(key: String): Any? {
        return this.valueForProperty(key)
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() {
            return mutableSetOf()
        }

    override val keys: MutableSet<String>
        get() {
            if (this.checkReleased()) { return mutableSetOf() }
            return (this.v8Value as? V8Object)?.keys?.toMutableSet() ?: mutableSetOf()
        }

    override val values: MutableCollection<Any>
        get() {
            return mutableListOf()
        }

    override fun clear() {

    }

    override fun put(key: String, value: Any): Any? {
        if (this.checkReleased()) { return value }
        this.setValueForProperty(value, key)
        return value
    }

    override fun putAll(from: Map<out String, Any>) {
        from.forEach { this.put(it.key, it.value) }
    }

    override fun remove(key: String): Any? {
        if (this.checkReleased()) { return null }
        val value = this.valueForProperty(key)
        (this.v8Value as? V8Object)?.addUndefined(key)
        return value
    }

    companion object {

        fun valueWithBool(value: Boolean, context: JSContext): JSValue {
            return JSValue(value, context)
        }

        fun valueWithDouble(value: Double, context: JSContext): JSValue {
            return JSValue(value, context)
        }

        fun valueWithInt(value: Int, context: JSContext): JSValue {
            return JSValue(value, context)
        }

        fun valueWithNewObjectInContext(context: JSContext): JSValue {
            if (context.runtime.isReleased) { return JSValue(null, context) }
            return JSValue(V8Object(context.runtime), context)
        }

        fun valueWithNewArrayInContext(context: JSContext): JSValue {
            if (context.runtime.isReleased) { return JSValue(null, context) }
            return JSValue(V8Array(context.runtime), context)
        }

        fun valueWithNewErrorFromMessage(message: String, context: JSContext): JSValue {
            if (context.runtime.isReleased) { return JSValue(null, context) }
            return try {
                JSValue(context.runtime.executeObjectScript("new Error('${message.replace("'", "\'")}')"), context)
            } catch (e: Exception) {
                JSValue(null, context)
            }
        }

        fun valueWithNullInContext(context: JSContext): JSValue {
            return JSValue(null, context)
        }

        fun valueWithUndefinedInContext(context: JSContext): JSValue {
            return JSValue(V8.getUndefined(), context)
        }

    }

}