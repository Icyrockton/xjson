package com.icyrockton.xjson.runtime.json

import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.encoding.AbstractEncoder
import com.icyrockton.xjson.runtime.encoding.CompositeEncoder

internal class JsonEncoder(val composer: JsonComposer, val json: XJson, val mode: JsonWriteMode) : AbstractEncoder() {
    constructor(writer: JsonWriter, json: XJson, mode: JsonWriteMode) : this(JsonComposer(writer), json, mode)

    override fun beginStructure(descriptor: Descriptor): CompositeEncoder {
        val newMode = json.switchMode(descriptor)
        composer.print(newMode.begin)
        composer.indent()
        if (mode == newMode) return this
        return JsonEncoder(composer, json, mode)
    }

    override fun endStructure(descriptor: Descriptor) {
        composer.unIndent()
        composer.nextItem()
        composer.print(mode.end)
    }

    override fun encodeElement(descriptor: Descriptor, index: Int): Boolean {
        when (mode) {
            JsonWriteMode.LIST -> {
                // TODO
            }

            JsonWriteMode.MAP -> {
                // TODO
            }

            JsonWriteMode.OBJ -> {
                // print element key
                if(!composer.writingFirst)
                    composer.print(COMMA)
                val name = descriptor.getElementName(index)
                composer.nextItem()
                composer.print(name)
                composer.print(COLON)
                composer.space()
            }
        }
        return true
    }

    override fun encodeNull() {
        composer.print(NULL)
    }

    override fun encodeBoolean(value: Boolean) {
        composer.print(value)
    }

    override fun encodeByte(value: Byte) {
        composer.print(value)
    }

    override fun encodeShort(value: Short) {
        composer.print(value)
    }

    override fun encodeInt(value: Int) {
        composer.print(value)
    }

    override fun encodeLong(value: Long) {
        composer.print(value)
    }

    override fun encodeFloat(value: Float) {
        composer.print(value)
    }

    override fun encodeDouble(value: Double) {
        composer.print(value)
    }

    override fun encodeChar(value: Char) {
        composer.print(value)
    }

    override fun encodeString(value: String) {
        composer.print(value)
    }

    override fun <T> encodeSerializableValue(serializer: XSerialization<T>, value: T) {
        serializer.serialize(this, value)
    }
}