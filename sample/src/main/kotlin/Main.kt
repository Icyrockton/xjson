package org.example

import com.icyrockton.xjson.runtime.annotation.XSerializable
import com.icyrockton.xjson.runtime.json.XJson

@XSerializable
data class Box<T>(
    val v: T
)

@XSerializable
data class Foo<T, V>(
    val a: T,
    val b: V,
    val box: Box<T>
)

fun encode() {
    val foo = Foo("hello", 20, Box("hello"))
    println(XJson { }.encodeToString(foo))
}

fun decode() {
    val str = """
        {
           "a":"this is a",
           "b":123.456,
           "box":{
              "v":"this is v"
           }
        }
    """.trimIndent()
    println(XJson { }.decodeFromString<Foo<String, Double>>(str))
}

fun main() {
    encode()
    decode()
}