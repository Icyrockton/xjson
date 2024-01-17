package com.icyrockton.xjson.runtime.json

import com.icyrockton.xjson.runtime.XDeSerialization
import com.icyrockton.xjson.runtime.XSerialization

class XJson {
    fun <T> encodeToString(serializer: XSerialization<T>, value: T): String {
        val writer = StringBuilderJsonWriter()
        val encoder = JsonEncoder(writer, this, JsonWriteMode.OBJ)
        encoder.encodeSerializableValue(serializer, value)
        return writer.toString()
    }

    fun <T> decodeFromString(deserializer: XDeSerialization<T>): T {
        TODO()
    }
}

fun XJson(builder: XJson.() -> Unit = { }): XJson {
    val xjson = XJson()
    xjson.builder()
    return xjson
}