package com.icyrockton.xjson.runtime.encoding

import com.icyrockton.xjson.runtime.XDeSerialization
import com.icyrockton.xjson.runtime.descriptor.Descriptor

abstract class AbstractDecoder : Decoder, CompositeDecoder {
    final override fun decodeBooleanElement(descriptor: Descriptor, index: Int): Boolean = decodeBoolean()
    final override fun decodeByteElement(descriptor: Descriptor, index: Int): Byte = decodeByte()
    final override fun decodeShortElement(descriptor: Descriptor, index: Int): Short = decodeShort()
    final override fun decodeIntElement(descriptor: Descriptor, index: Int): Int = decodeInt()
    final override fun decodeLongElement(descriptor: Descriptor, index: Int): Long = decodeLong()
    final override fun decodeFloatElement(descriptor: Descriptor, index: Int): Float = decodeFloat()
    final override fun decodeDoubleElement(descriptor: Descriptor, index: Int): Double = decodeDouble()
    final override fun decodeCharElement(descriptor: Descriptor, index: Int): Char = decodeChar()
    final override fun decodeStringElement(descriptor: Descriptor, index: Int): String = decodeString()
    final override fun <T> decodeSerializableElement(
        descriptor: Descriptor,
        index: Int,
        deserialize: XDeSerialization<T>
    ): T = decodeSerializableValue(deserialize)
}