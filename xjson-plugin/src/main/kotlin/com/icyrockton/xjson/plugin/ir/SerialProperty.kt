package com.icyrockton.xjson.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

data class SerialProperty(
    val ir: IrProperty,
    val name: Name,
    val type: IrType,
)

class SerialProperties(
    val properties: List<SerialProperty>,
) {


    companion object {
        fun findSerialPropertiesInClass(irClass: IrClass): SerialProperties {
            // current we only collect property in primary construct
            val primaryConstructor = irClass.primaryConstructor ?: error("primary constructor not found for $irClass")
            val allProperty = irClass.properties.map { it.name to it }
            val primaryConstructorProperties = primaryConstructor.valueParameters.map { valueParameter ->
                val findProperty = allProperty.find { it.first == valueParameter.name }
                if (findProperty == null)
                    error("property ${valueParameter.name} not found in class $irClass")
                SerialProperty(findProperty.second, findProperty.first, findProperty.second.getter!!.returnType)
            }
            return SerialProperties(primaryConstructorProperties)
        }
    }
}