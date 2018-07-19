package com.xt.jscore

import com.eclipsesource.v8.Releasable
import com.eclipsesource.v8.V8Value

/**
 * Created by cuiminghui on 2018/7/19.
 */
class JSValue(val managedValue: Any) {

    fun finalize() {
        (managedValue as? Releasable)?.release()
    }

}