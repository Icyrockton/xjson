// FIR_DUMP
// DUMP_IR

import com.icyrockton.xjson.runtime.annotation.XSerializable

@XSerializable
data class Foo(val a: Double, val d: String)

@XSerializable
data class Box<T, V>(val t: T, val v: V)
