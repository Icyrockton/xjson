package com.icyrockton.xjson.runtime.json

import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.descriptor.StructureKind
import com.icyrockton.xjson.runtime.json.lexer.BEGIN_LIST
import com.icyrockton.xjson.runtime.json.lexer.BEGIN_OBJ
import com.icyrockton.xjson.runtime.json.lexer.END_LIST
import com.icyrockton.xjson.runtime.json.lexer.END_OBJ

internal enum class JsonWriteMode(val begin: Char, val end: Char) {
    OBJ(BEGIN_OBJ, END_OBJ),
    MAP(BEGIN_OBJ, END_OBJ),
    LIST(BEGIN_LIST, END_LIST)
}

internal fun XJson.switchMode(descriptor: Descriptor): JsonWriteMode =
    when (descriptor.kind) {
        StructureKind.MAP -> JsonWriteMode.MAP
        StructureKind.LIST -> JsonWriteMode.LIST
        else -> JsonWriteMode.OBJ
    }
