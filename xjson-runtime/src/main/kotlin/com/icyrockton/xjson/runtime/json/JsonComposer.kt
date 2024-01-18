package com.icyrockton.xjson.runtime.json

open class JsonComposer(private val writer: JsonWriter) {
    var writingFirst = true
    fun space() = print(' ')
    fun print(v: Char) = writer.writeChar(v)
    fun print(v: String) = writer.write(v)
    fun print(v: Boolean) = writer.write(v.toString())
    fun print(v: Byte) = writer.writeLong(v.toLong())
    fun print(v: Short) = writer.writeLong(v.toLong())
    fun print(v: Int) = writer.writeLong(v.toLong())
    fun print(v: Long) = writer.writeLong(v)
    fun print(v: Float) = writer.write(v.toString())
    fun print(v: Double) = writer.write(v.toString())
    fun printQuoted(v: String) = writer.writeQuoted(v)
    open fun indent() {
        writingFirst = true
    }

    open fun unIndent() {

    }

    /**
     * used for pretty print
     */
    open fun nextItem() {
        writingFirst = false
    }
}