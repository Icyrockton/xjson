// FIR_DUMP
// DUMP_IR

import com.icyrockton.xjson.runtime.annotation.XSerializable


@XSerializable
data class Box<T>(
    val v : T
)

@XSerializable
data class Foo<T,V>(
    val a: T,
    val b: V,
    val box: Box<T>
)