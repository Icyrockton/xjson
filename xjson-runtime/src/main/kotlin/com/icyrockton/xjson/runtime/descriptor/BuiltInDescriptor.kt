package com.icyrockton.xjson.runtime.descriptor

internal class PrimitiveDescriptor(
    override val name: String,
    override val kind: PrimitiveKind
) : Descriptor {
    override val elementsCount: Int = 0
    override fun getElementName(index: Int): String = error()
    override fun getElementIndex(name: String): Int = error()
    override fun getElementDescriptor(index: Int): Descriptor = error()
    override fun getElementIsOptional(index: Int): Boolean = error()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrimitiveDescriptor) return false
        if (name != other.name) return false
        return kind == other.kind
    }
    override fun hashCode(): Int =
        name.hashCode() + 31 * kind.hashCode()
    override fun toString(): String = "PrimitiveDescriptor(${name},${kind})"
    private fun error(): Nothing = throw IllegalStateException("Primitive descriptor does not have elements")
}