package com.icyrockton.xjson.runtime.json

import com.icyrockton.xjson.runtime.json.lexer.STRING

interface JsonWriter {
    fun writeChar(v: Char)
    fun writeLong(v: Long)
    fun write(v: String)
    fun writeQuoted(v: String)
    override fun toString(): String
}

class StringBuilderJsonWriter(
    private val builder: StringBuilder
) : JsonWriter {

    constructor(): this(java.lang.StringBuilder())
    override fun writeChar(v: Char) {
        builder.append(v)
    }

    override fun writeLong(v: Long) {
        builder.append(v)
    }

    override fun write(v: String) {
        builder.append(v)
    }

    override fun writeQuoted(v: String) {
        builder.append(STRING)
        builder.append(v)
        builder.append(STRING)
    }

    override fun toString(): String {
        return builder.toString()
    }
}