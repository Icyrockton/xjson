package com.icyrockton.xjson.plugin.ir

import com.icyrockton.xjson.plugin.XJsonClassId.compositeDecoderClassId
import com.icyrockton.xjson.plugin.XJsonClassId.compositeEncoderClassId
import com.icyrockton.xjson.plugin.XJsonClassId.decoderClassId
import com.icyrockton.xjson.plugin.XJsonClassId.encoderClassId
import com.icyrockton.xjson.plugin.XJsonPackage
import com.icyrockton.xjson.plugin.XJsonPluginGenerated
import com.icyrockton.xjson.plugin.XJsonPluginKey
import com.icyrockton.xjson.plugin.irOrigin
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.withReferenceScope
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

data class SerialPropertySerialInfo(
    val encodeType: String? = null,    // encodeXXXXElement
    val serializer: IrClassSymbol? = null,
    val isGeneric: Boolean = false
)


abstract class AbstractIrGenerator(protected val pluginContext: IrPluginContext) {
    private val irBuiltIns = pluginContext.irBuiltIns
    fun IrFunction.addFunctionBody(blockBuilder: IrBlockBodyBuilder.() -> Unit) {
        val builder = pluginContext.irBuiltIns.createIrBuilder(symbol, startOffset, endOffset)
        body = builder.irBlockBody(body = blockBuilder)
    }

    val compositeEncoderClass = referenceClass(compositeEncoderClassId)
    val encoderClass = referenceClass(encoderClassId)
    val decoderClass = referenceClass(decoderClassId)
    val compositeDecoderClass = referenceClass(compositeDecoderClassId)

    protected fun referenceClass(classId: ClassId): IrClass {
        return pluginContext.referenceClass(classId)?.owner
            ?: error("Class $classId not found, check your runtime dependency")
    }

    fun findSerialInfoForProperty(property: SerialProperty): SerialPropertySerialInfo? {
        val type = property.type
        return when {
            type.isTypeParameter() -> {
                return SerialPropertySerialInfo(
                    isGeneric = true
                )
            }

            type.isPrimitiveType() -> {
                // call encodeXXXElement directly
                return SerialPropertySerialInfo(
                    encodeType = type.getPrimitiveType()!!.typeName.asString(),
                )
            }
            // TODO array
            // TODO string
            else -> {
                return SerialPropertySerialInfo(
                    serializer = findSerializerByType(type)
                )
            }
        }
    }

    fun findSerializerByType(type: IrType): IrClassSymbol? {
        // type parameter
        if (type.isTypeParameter()) return null

        // primitive
        findPrimitiveSerializerByType(type)?.let { return it }

        // generated serializer
        return type.classOrNull?.owner?.getGenerateSerializerClass()?.symbol
    }

    fun findPrimitiveSerializerByType(type: IrType): IrClassSymbol? {
        val typeName = type.classFqName?.asString()
        val serializerName = when (typeName) {
            "kotlin.Boolean" -> "BooleanSerializer"
            "kotlin.Byte" -> "ByteSerializer"
            "kotlin.Short" -> "ShortSerializer"
            "kotlin.Int" -> "IntSerializer"
            "kotlin.Long" -> "LongSerializer"
            "kotlin.Float" -> "FloatSerializer"
            "kotlin.Double" -> "DoubleSerializer"
            "kotlin.Char" -> "CharSerializer"
            "kotlin.String" -> "StringSerializer"
            null -> null
            else -> null
        } ?: return null

        return referenceClass(ClassId(XJsonPackage.corePackage, Name.identifier(serializerName))).symbol
    }


    fun IrBuilderWithScope.instantiateSerializer(
        serializer: IrClassSymbol?,
        type: IrType,
        getGenericSerializerByIndex: (Int) -> IrExpression
    ): IrExpression {
        val type = type as IrSimpleType
        if (type.isTypeParameter()) {
            val genericIndex = (type.classifierOrNull as IrTypeParameterSymbol).owner.index
            return getGenericSerializerByIndex(genericIndex)
        }
        val clazz = serializer!!.owner
        if (clazz.kind == ClassKind.OBJECT) return irGetObject(serializer)

        val ctr =
            findTypeParameterConstructorForSerializer(clazz)
                ?: error("not found typeParameter constructor for ${clazz.fqNameWhenAvailable}")


        // populate type argument Serializer
        val typeArgumentType = type.arguments.map { it.typeOrFail }
        val args = typeArgumentType.map {
            val serializer = findSerializerByType(it)
            instantiateSerializer(serializer, it, getGenericSerializerByIndex)
        }

        return irCall(ctr).apply {
            args.forEachIndexed { index, irExpression ->
                putValueArgument(index, irExpression)
            }

            typeArgumentType.forEachIndexed { index, irType ->
                putTypeArgument(index, irType)
            }
        }
    }

    private fun findTypeParameterConstructorForSerializer(irClass: IrClass): IrConstructorSymbol? {
        return irClass.constructors.singleOrNull {
            it.valueParameters.size == irClass.typeParameters.size
                    && it.valueParameters.all {it.type.isXSerializer  }
        }?.symbol
    }


    fun <T : IrDeclaration> T.buildWithScope(builder: (T) -> Unit): T =
        also { irDeclaration ->
            pluginContext.symbolTable.withReferenceScope(irDeclaration) {
                builder(irDeclaration)
            }
        }

    fun IrClass.addAnonymousInit(body: IrBlockBodyBuilder.() -> Unit) {
        val anonymousInit = this.run {
            val symbol = IrAnonymousInitializerSymbolImpl(symbol)
            this.factory.createAnonymousInitializer(startOffset, endOffset, XJsonPluginKey.irOrigin, symbol).also {
                it.parent = this
                declarations.add(it)
            }
        }

        anonymousInit.buildWithScope { initIrBody ->
            initIrBody.body =
                DeclarationIrBuilder(
                    pluginContext,
                    initIrBody.symbol,
                    initIrBody.startOffset,
                    initIrBody.endOffset
                ).irBlockBody(body = body)
        }
    }


    fun IrBuilderWithScope.irArrayOf(
        elementType: IrType,
        elements: List<IrExpression>,
    ): IrExpression {
        val arrayType = irBuiltIns.arrayClass.typeWith(
            elementType
        )

        val arg = IrVarargImpl(startOffset, endOffset, arrayType, elementType, elements)
        return irCall(irBuiltIns.arrayOf, arrayType, typeArguments = listOf(elementType)).apply {
            putValueArgument(0, arg)
        }
    }
}