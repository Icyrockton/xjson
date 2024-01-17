package com.icyrockton.xjson.runtime.encoding

import com.icyrockton.xjson.runtime.XDeSerialization
import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.descriptor.Descriptor

interface Decoder {
    fun decodeNull(): Nothing?
    fun decodeBoolean(): Boolean
    fun decodeByte(): Byte
    fun decodeShort(): Short
    fun decodeInt(): Int
    fun decodeLong(): Long
    fun decodeFloat(): Float
    fun decodeDouble(): Double
    fun decodeChar(): Char
    fun decodeString(): String
    fun <T> decodeSerializableValue(deserializer: XDeSerialization<T>): T
    fun beginStructure(descriptor: Descriptor): CompositeDecoder

}

interface CompositeDecoder {
    companion object {
        val DECODE_TERMINAL = -1
    }

    fun endStructure(descriptor: Descriptor)

    /**
     * return current element index
     * If the return value is [DECODE_TERMINAL], the structure has reached its end, and we should call [endStructure].
     */
    fun decodeElementIndex(descriptor: Descriptor): Int
    fun decodeBooleanElement(descriptor: Descriptor, index: Int): Boolean
    fun decodeByteElement(descriptor: Descriptor, index: Int): Byte
    fun decodeShortElement(descriptor: Descriptor, index: Int): Short
    fun decodeIntElement(descriptor: Descriptor, index: Int): Int
    fun decodeLongElement(descriptor: Descriptor, index: Int): Long
    fun decodeFloatElement(descriptor: Descriptor, index: Int): Float
    fun decodeDoubleElement(descriptor: Descriptor, index: Int): Double
    fun decodeCharElement(descriptor: Descriptor, index: Int): Char
    fun decodeStringElement(descriptor: Descriptor, index: Int): String
    fun <T> decodeSerializableElement(descriptor: Descriptor, index: Int, deserialize: XDeSerialization<T>): T
}

inline fun Decoder.beginStructure(descriptor: Descriptor, crossinline block: CompositeDecoder.() -> Unit) {
    val compositeDecoder = this.beginStructure(descriptor)
    compositeDecoder.block()
    compositeDecoder.endStructure(descriptor)
}

