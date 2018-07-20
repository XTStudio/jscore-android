package com.xt.jscore

import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.utils.V8ObjectUtils

/**
 * Created by cuiminghui on 2018/7/20.
 */
interface JSExport {
}

internal fun JSExportCreateV8Object(obj: JSExport, context: JSContext): V8Object {
    val v8Object = V8Object(context.runtime)
    var interfaces = mutableListOf<Class<*>>()
    var currentClass: (Class<*>)? = obj::class.java
    do {
        currentClass!!.interfaces.forEach {
            if (JSExport::class.java.isAssignableFrom(it)) {
                interfaces.add(it)
            }
        }
        currentClass = currentClass!!.superclass
    } while (currentClass != null)
    interfaces.forEach {
        it.methods.forEach { method ->
            v8Object.registerJavaMethod({ _, v8Array ->
                method.invoke(obj, *V8ObjectUtils.toList(v8Array).toTypedArray())
            }, method.name)
        }
    }
    return v8Object
}