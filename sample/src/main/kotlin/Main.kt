package org.example

import com.icyrockton.xjson.runtime.annotation.XSerializable
import com.icyrockton.xjson.runtime.core.serializer
import com.icyrockton.xjson.runtime.json.XJson

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

fun main() {
    val foo = Foo("hello",20,Box("hello"))
    println(XJson { }.encodeToString(foo))
}