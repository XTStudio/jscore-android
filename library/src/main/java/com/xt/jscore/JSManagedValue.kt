package com.xt.jscore

import com.eclipsesource.v8.V8Value
import java.lang.ref.WeakReference

/**
 * Created by cuiminghui on 2018/7/20.
 */
class JSManagedValue(value: JSValue, owner: Any? = null) {

    private var managedValue: JSValue? = null
    private var owner: WeakReference<Any>? = null

    var value: JSValue? = null
        get() {
            if (owner != null && owner?.get() == null) {
                this.managedValue = null
                return null
            }
            return this.managedValue
        }
        private set

    init {
        if (owner != null) {
            val strongValue: Any? = (value.v8Value as? V8Value)?.twin() ?: value.v8Value
            this.managedValue = JSValue(strongValue, value.context)
            this.owner = WeakReference(owner)
        }
        else {
            val weakValue: Any? = (value.v8Value as? V8Value)?.twin()?.setWeak() ?: value.v8Value
            this.managedValue = JSValue(weakValue, value.context)
        }
    }

}