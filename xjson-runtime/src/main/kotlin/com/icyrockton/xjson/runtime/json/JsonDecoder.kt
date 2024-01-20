package com.icyrockton.xjson.runtime.json

import com.icyrockton.xjson.runtime.XDeSerialization
import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.encoding.AbstractDecoder
import com.icyrockton.xjson.runtime.encoding.CompositeDecoder

internal class JsonDecoder(val lexer: JsonLexer, val json: XJson, val mode: JsonWriteMode) : AbstractDecoder() {
    override fun beginStructure(descriptor: Descriptor): CompositeDecoder {
        val newMode = json.switchMode(descriptor)
        lexer.expectNextToken(newMode.begin)
        if (newMode == mode)
            return this
        return JsonDecoder(lexer, json, mode)
    }

    override fun endStructure(descriptor: Descriptor) {
        lexer.expectNextToken(mode.end)
    }

    override fun decodeElementIndex(descriptor: Descriptor): Int {
        lexer.tryConsumeComma()
        if (!lexer.canConsumeValue()) return Descriptor.UNKNOWN_ELEMENT
        val key = lexer.consumeStringKey()
        lexer.expectNextToken(COLON)
        return descriptor.getElementIndex(key)
    }

    override fun decodeNull(): Nothing? {
        TODO("Not yet implemented")
    }

    override fun decodeBoolean(): Boolean {
        return lexer.consumeBoolean()
    }

    override fun decodeByte(): Byte {
        val value = lexer.consumeNumeric()
        if (value.toByte().toLong() != value) error("expected Byte, but found $value")
        return value.toByte()
    }

    override fun decodeShort(): Short {
        val value = lexer.consumeNumeric()
        if (value.toShort().toLong() != value) error("expected Short, but found $value")
        return value.toShort()
    }

    override fun decodeInt(): Int {
        val value = lexer.consumeNumeric()
        if (value.toInt().toLong() != value) error("expected Int, but found $value")
        return value.toInt()
    }

    override fun decodeLong(): Long {
        val value = lexer.consumeNumeric()
        return value
    }

    override fun decodeFloat(): Float {
        return lexer.consumeFloat()
    }

    override fun decodeDouble(): Double {
        return lexer.consumeDouble()
    }

    override fun decodeChar(): Char {
        return lexer.consumeNextToken()
    }

    override fun decodeString(): String {
        return lexer.consumeString()
    }

    override fun <T> decodeSerializableValue(deserializer: XDeSerialization<T>): T {
        return deserializer.deserialize(this)
    }
}