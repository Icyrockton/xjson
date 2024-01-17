package com.icyrockton.xjson.runtime.encoding

import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.core.serializer
import com.icyrockton.xjson.runtime.descriptor.Descriptor

interface Encoder {
    fun encodeNull()
    fun encodeBoolean(value: Boolean)
    fun encodeByte(value: Byte)
    fun encodeShort(value: Short)
    fun encodeInt(value: Int)
    fun encodeLong(value: Long)
    fun encodeFloat(value: Float)
    fun encodeDouble(value: Double)
    fun encodeChar(value: Char)
    fun encodeString(value: String)
    fun <T> encodeSerializableValue(serializer: XSerialization<T>, value: T)
    fun beginStructure(descriptor: Descriptor): CompositeEncoder
}

/**
 * encode structural elements
 */
interface CompositeEncoder {
    fun endStructure(descriptor: Descriptor)
    fun encodeBooleanElement(descriptor: Descriptor, index: Int, value: Boolean)
    fun encodeByteElement(descriptor: Descriptor, index: Int, value: Byte)
    fun encodeShortElement(descriptor: Descriptor, index: Int, value: Short)
    fun encodeIntElement(descriptor: Descriptor, index: Int, value: Int)
    fun encodeFloatElement(descriptor: Descriptor, index: Int, value: Float)
    fun encodeDoubleElement(descriptor: Descriptor, index: Int, value: Double)
    fun encodeCharElement(descriptor: Descriptor, index: Int, value: Char)
    fun encodeStringElement(descriptor: Descriptor, index: Int, value: String)
    fun <T> encodeSerializableElement(descriptor: Descriptor, index: Int, serializer: XSerialization<T>, value: T)

}

inline fun Encoder.beginStructure(descriptor: Descriptor, crossinline block: CompositeEncoder.() -> Unit) {
    val compositeEncoder = this.beginStructure(descriptor)
    compositeEncoder.block()
    compositeEncoder.endStructure(descriptor)
}
