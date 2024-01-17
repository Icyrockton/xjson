package com.icyrockton.xjson.runtime.descriptor

/**
 * [Descriptor] describe the data model, including child element key names and whether a child element is optional
 */
interface Descriptor {
    /**
     * e.g. com.icyrockton.Foo
     */
    val name: String

    val kind: DescriptorKind

    val isNullable: Boolean
        get() = false

    /**
     * child element count
     */
    val elementsCount: Int

    fun getElementName(index: Int): String
    fun getElementIndex(name: String): Int
    fun getElementDescriptor(index: Int): Descriptor
    fun getElementIsOptional(index: Int): Boolean
}


