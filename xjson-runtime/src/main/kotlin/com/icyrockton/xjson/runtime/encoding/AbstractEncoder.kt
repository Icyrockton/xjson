package com.icyrockton.xjson.runtime.encoding

import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.descriptor.Descriptor

abstract class AbstractEncoder : Encoder, CompositeEncoder {
    public abstract fun encodeElement(descriptor: Descriptor, index: Int): Boolean
    final override fun encodeBooleanElement(descriptor: Descriptor, index: Int, value: Boolean) {
        if (encodeElement(descriptor, index)) encodeBoolean(value)
    }
    final override fun encodeByteElement(descriptor: Descriptor, index: Int, value: Byte) {
        if (encodeElement(descriptor, index)) encodeByte(value)
    }
    final override fun encodeShortElement(descriptor: Descriptor, index: Int, value: Short) {
        if (encodeElement(descriptor, index)) encodeShort(value)
    }
    final override fun encodeIntElement(descriptor: Descriptor, index: Int, value: Int) {
        if (encodeElement(descriptor, index)) encodeInt(value)
    }
    final override fun encodeFloatElement(descriptor: Descriptor, index: Int, value: Float) {
        if (encodeElement(descriptor, index)) encodeFloat(value)
    }
    final override fun encodeDoubleElement(descriptor: Descriptor, index: Int, value: Double) {
        if (encodeElement(descriptor, index)) encodeDouble(value)
    }
    final override fun encodeCharElement(descriptor: Descriptor, index: Int, value: Char) {
        if (encodeElement(descriptor, index)) encodeChar(value)
    }
    final override fun encodeStringElement(descriptor: Descriptor, index: Int, value: String) {
        if (encodeElement(descriptor, index)) encodeString(value)
    }
    final override fun <T> encodeSerializableElement(
        descriptor: Descriptor,
        index: Int,
        serializer: XSerialization<T>,
        value: T
    ) {
        if (encodeElement(descriptor, index)) encodeSerializableValue(serializer, value)
    }
}