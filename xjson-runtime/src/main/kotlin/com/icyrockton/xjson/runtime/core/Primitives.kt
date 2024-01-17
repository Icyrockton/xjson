package com.icyrockton.xjson.runtime.core

import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.XSerializer
import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.descriptor.PrimitiveDescriptor
import com.icyrockton.xjson.runtime.descriptor.PrimitiveKind
import com.icyrockton.xjson.runtime.encoding.Decoder
import com.icyrockton.xjson.runtime.encoding.Encoder
import kotlin.reflect.KClass


val PRIMITIVE_SERIALIZER = mapOf<KClass<*>,XSerialization<*>>(
    Boolean::class to Boolean.serializer(),
    Byte::class to Byte.serializer(),
    Short::class to Short.serializer(),
    Int::class to Int.serializer(),
    Long::class to Long.serializer(),
    Float::class to Float.serializer(),
    Double::class to Double.serializer(),
    Char::class to Char.serializer(),
    String::class to String.serializer(),
)

fun Boolean.Companion.serializer() : XSerializer<Boolean> = BooleanSerializer
fun Byte.Companion.serializer() : XSerializer<Byte> = ByteSerializer
fun Short.Companion.serializer() : XSerializer<Short> = ShortSerializer
fun Int.Companion.serializer() : XSerializer<Int> = IntSerializer
fun Long.Companion.serializer() : XSerializer<Long> = LongSerializer
fun Float.Companion.serializer() : XSerializer<Float> = FloatSerializer
fun Double.Companion.serializer() : XSerializer<Double> = DoubleSerializer
fun Char.Companion.serializer() : XSerializer<Char> = CharSerializer
fun String.Companion.serializer() : XSerializer<String> = StringSerializer

internal object BooleanSerializer : XSerializer<Boolean> {
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)
    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeBoolean()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Boolean", PrimitiveKind.BOOLEAN)
}

internal object ByteSerializer : XSerializer<Byte> {
    override fun serialize(encoder: Encoder, value: Byte) = encoder.encodeByte(value)
    override fun deserialize(decoder: Decoder): Byte = decoder.decodeByte()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Byte", PrimitiveKind.BYTE)
}

internal object ShortSerializer : XSerializer<Short> {
    override fun serialize(encoder: Encoder, value: Short) = encoder.encodeShort(value)
    override fun deserialize(decoder: Decoder): Short = decoder.decodeShort()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Byte", PrimitiveKind.SHORT)
}

internal object IntSerializer : XSerializer<Int> {
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)
    override fun deserialize(decoder: Decoder): Int = decoder.decodeInt()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Int", PrimitiveKind.INT)
}

internal object LongSerializer : XSerializer<Long> {
    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeLong(value)
    override fun deserialize(decoder: Decoder): Long = decoder.decodeLong()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Long", PrimitiveKind.LONG)
}

internal object FloatSerializer : XSerializer<Float> {
    override fun serialize(encoder: Encoder, value: Float) = encoder.encodeFloat(value)
    override fun deserialize(decoder: Decoder): Float = decoder.decodeFloat()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Float", PrimitiveKind.LONG)
}

internal object DoubleSerializer : XSerializer<Double> {
    override fun serialize(encoder: Encoder, value: Double) = encoder.encodeDouble(value)
    override fun deserialize(decoder: Decoder): Double = decoder.decodeDouble()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Double", PrimitiveKind.DOUBLE)
}

internal object CharSerializer : XSerializer<Char> {
    override fun serialize(encoder: Encoder, value: Char) = encoder.encodeChar(value)
    override fun deserialize(decoder: Decoder): Char = decoder.decodeChar()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.Char", PrimitiveKind.CHAR)
}

internal object StringSerializer : XSerializer<String> {
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String = decoder.decodeString()
    override val descriptor: Descriptor
        get() = PrimitiveDescriptor("Kotlin.String", PrimitiveKind.STRING)
}
