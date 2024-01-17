package com.icyrockton.xjson.runtime.descriptor

sealed class DescriptorKind {
}

sealed class PrimitiveKind : DescriptorKind() {
    data object BOOLEAN : PrimitiveKind()
    data object BYTE : PrimitiveKind()
    data object CHAR : PrimitiveKind()
    data object SHORT : PrimitiveKind()
    data object INT : PrimitiveKind()
    data object LONG : PrimitiveKind()
    data object FLOAT : PrimitiveKind()
    data object DOUBLE : PrimitiveKind()
    data object STRING : PrimitiveKind()
}

sealed class StructureKind : DescriptorKind() {
    data object OBJ : StructureKind()
    data object MAP : StructureKind()
    data object LIST : StructureKind()
}