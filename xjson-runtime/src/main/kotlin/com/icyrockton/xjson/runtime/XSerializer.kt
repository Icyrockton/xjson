package com.icyrockton.xjson.runtime

import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.encoding.Decoder
import com.icyrockton.xjson.runtime.encoding.Encoder

interface XSerialization<T> {
    val descriptor : Descriptor
    fun serialize(encoder: Encoder, value: T)
}

interface XDeSerialization<T> {
    val descriptor : Descriptor
    fun deserialize(decoder: Decoder): T
}


interface XSerializer<T> : XSerialization<T>, XDeSerialization<T> {
    override val descriptor: Descriptor

}

interface PluginGeneratedXSerializer<T> : XSerializer<T> {
    /**
     * used for TypeParameter
     */
    fun typeParameterSerializers() : Array<XSerializer<*>> = emptyArray()
}