package com.icyrockton.xjson.plugin.ir

import com.icyrockton.xjson.plugin.XJsonClassId.missingPropertyExceptionClassId
import com.icyrockton.xjson.plugin.XJsonClassId.pluginGeneratedDescriptor
import com.icyrockton.xjson.plugin.XJsonClassId.serializerClassId
import com.icyrockton.xjson.plugin.XJsonNames.BEGIN_STRUCTURE
import com.icyrockton.xjson.plugin.XJsonNames.CHILD_SERIALIZERS
import com.icyrockton.xjson.plugin.XJsonNames.DECODE_ELEMENT_INDEX
import com.icyrockton.xjson.plugin.XJsonNames.DESCRIPTOR
import com.icyrockton.xjson.plugin.XJsonNames.DESERIALIZE
import com.icyrockton.xjson.plugin.XJsonNames.END_STRUCTURE
import com.icyrockton.xjson.plugin.XJsonNames.SERIALIZE
import com.icyrockton.xjson.plugin.XJsonNames.TYPE_PARAMETER_SERIALIZERS
import com.icyrockton.xjson.plugin.XJsonNames.TYPE_PARAMETER_SERIALIZER_VALUE_PARAMETER_NAME
import com.icyrockton.xjson.plugin.XJsonPluginKey
import com.icyrockton.xjson.plugin.irOrigin
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addDefaultGetter
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrInstanceInitializerCall
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class IrSerializerGenerator(val irClass: IrClass, pluginContext: IrPluginContext) : AbstractIrGenerator(pluginContext) {
    private val serializableClass = irClass.serializableClass
    private val descriptor =
        irClass.findPluginGeneratePropertyByName(DESCRIPTOR) ?: error("Not found descriptor for $irClass")

    private val serialProperty = SerialProperties.findSerialPropertiesInClass(serializableClass)
    private val serializerClassSymbol = referenceClass(serializerClassId).symbol
    private val localTypeParameterSerializers: List<IrProperty> = run {
        val result = mutableListOf<IrProperty>()
        irClass.typeParameters.forEachIndexed { index, tp ->

            val propertyType = serializerClassSymbol.createType(false, listOf(tp.defaultType))

            val prop = irClass.addProperty {
                name = TYPE_PARAMETER_SERIALIZER_VALUE_PARAMETER_NAME(index)
                origin = XJsonPluginKey.irOrigin
                visibility = DescriptorVisibilities.PRIVATE
                isVar = false
                startOffset = irClass.startOffset
                endOffset = irClass.endOffset
            }

            val field = prop.addBackingField {
                startOffset = irClass.startOffset
                endOffset = irClass.endOffset
                type = serializerClassSymbol.createType(false, listOf(tp.defaultType))
            }

            prop.addGetter {
                startOffset = irClass.startOffset
                endOffset = irClass.endOffset
                returnType = propertyType
                origin = XJsonPluginKey.irOrigin
                modality = Modality.FINAL
            }.apply {
                dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(this, type = irClass.defaultType)
                body = pluginContext.irBuiltIns.createIrBuilder(symbol, irClass.startOffset, irClass.endOffset)
                    .irBlockBody {
                        +irReturn(irGetField(irGet(dispatchReceiverParameter!!), field))
                    }
            }

            result += prop
        }
        result
    }

    /**
     *  $serializer$.serialize(encoder: Encoder, value: T)
     */
    private fun generateSerialize(function: IrSimpleFunction) = function.addFunctionBody {
        fun irThis() = irGet(function.dispatchReceiverParameter!!)  // IrElement should not be unique
        val descriptorGetter = descriptor.getter!!.symbol
        // val descriptor = this.descriptor
        val descriptor =
            irTemporary(irGet(descriptorGetter.owner.returnType, irThis(), descriptorGetter), nameHint = "descriptor")

        val encoder = function.valueParameters[0]
        val value = function.valueParameters[1]

        val encoderBegin = encoderClass.functions.single { it.name == BEGIN_STRUCTURE && it.valueParameters.size == 1 }

        val call = irCall(encoderBegin).apply {
            dispatchReceiver = irGet(encoder)
            putValueArgument(0, irGet(descriptor))
        }

        // val cEncoder = encoder.beginStructure(descriptor)
        val cEncoder = irTemporary(call, nameHint = "cEncoder")

        fun SerialProperty.irGet() = irGet(type, irGet(value), ir.getter!!.symbol)

        serialProperty.properties.forEachIndexed { index, serialProperty ->

            val errorMsg =
                "cannot determine which encode function should be called for property ${serialProperty.name}: ${serialProperty.type}"
            val serialInfo = findSerialInfoForProperty(serialProperty)
                ?: error(errorMsg)
            val type = serialProperty.type
            when {
                serialInfo.serializer != null || serialInfo.isGeneric -> {
                    val serializer = instantiateSerializer(serialInfo.serializer, type) { genericIndex ->
                        irGetField(irThis(), localTypeParameterSerializers[genericIndex].backingField!!)
                    }

                    val encodeElementFunction =
                        compositeEncoderClass.functions.single { it.name.asString() == "encodeSerializableElement" && it.valueParameters.size == 4 }

                    +irCall(encodeElementFunction).apply {
                        dispatchReceiver = irGet(cEncoder)
                        putValueArgument(0, irGet(descriptor))
                        putValueArgument(1, irInt(index))
                        putValueArgument(2, serializer)   // serializer instance
                        putValueArgument(3, irGet(serialProperty.type, irGet(value), serialProperty.ir.getter!!.symbol))
                    }
                }

                serialInfo.encodeType != null -> {
                    val encodeFunc =
                        compositeEncoderClass.functions.single { it.name.asString() == "encode${serialInfo.encodeType}Element" }
                    +irCall(encodeFunc).apply {
                        dispatchReceiver = irGet(cEncoder)
                        putValueArgument(0, irGet(descriptor))
                        putValueArgument(1, irInt(index))
                        putValueArgument(2, serialProperty.irGet())
                    }
                }

                else -> error(errorMsg)
            }
        }

        val encoderEnd =
            compositeEncoderClass.functions.single { it.name == END_STRUCTURE && it.valueParameters.size == 1 }
        +irCall(encoderEnd).apply {
            dispatchReceiver = irGet(cEncoder)
            putValueArgument(0, irGet(descriptor))
        }
    }

    /**
     *  $serializer$.deserialize(decoder: Decoder): T
     */
    fun generateDeserialize(function: IrSimpleFunction) = function.addFunctionBody {
        fun irThis() = irGet(function.dispatchReceiverParameter!!)  // IrElement should not be unique
        val decoder = function.valueParameters[0]
        val descriptor =
            irTemporary(irGet(descriptor.getter!!.returnType, irThis(), descriptor.getter!!.symbol), "descriptor")

        val decoderBegin = decoderClass.functions.single { it.name == BEGIN_STRUCTURE && it.valueParameters.size == 1 }
        val decodeElementIndexFunc =
            compositeDecoderClass.functions.single { it.name == DECODE_ELEMENT_INDEX && it.valueParameters.size == 1 }


        val call = irCall(decoderBegin).apply {
            dispatchReceiver = irGet(decoder)
            putValueArgument(0, irGet(descriptor))
        }

        // val cDecoder = decoder.beginStructure(descriptor)
        val cDecoder = irTemporary(call, nameHint = "cDecoder")

        // create local var
        val localVars = serialProperty.properties.mapIndexed { index, serialProperty ->
            val local = irTemporary(irNull(), "prop_${index}", serialProperty.type.makeNullable(), isMutable = true)
            serialProperty to local
        }.toMap()

        +irWhile().apply {
            val loop: IrLoop = this
            condition = irTrue()
            body = irBlock {
                // val idx = cDecoder.decodeElementIndex(descriptor)
                val callDecodeElementIndex = irCall(decodeElementIndexFunc).apply {
                    dispatchReceiver = irGet(cDecoder)
                    putValueArgument(0, irGet(descriptor))
                }
                val idx = irTemporary(callDecodeElementIndex, nameHint = "idx")

                val branches = serialProperty.properties.mapIndexed { index, serialProperty ->
                    val condition = irEquals(irGet(idx), irInt(index))
                    val localVar =
                        localVars[serialProperty] ?: error("not found local var for property ${serialProperty.name}")
                    val errorMsg =
                        "cannot determine which encode function should be called for property ${serialProperty.name}: ${serialProperty.type}"
                    val serialInfo = findSerialInfoForProperty(serialProperty)
                        ?: error(errorMsg)

                    val callDecode = when {
                        serialInfo.serializer != null || serialInfo.isGeneric -> {
                            val serializer = instantiateSerializer(serialInfo.serializer, serialProperty.type) { genericIndex ->
                                irGetField(irThis(), localTypeParameterSerializers[genericIndex].backingField!!)
                            }

                            val decodeFunc =
                                compositeDecoderClass.functions.single { it.name.asString() == "decodeSerializableElement" && it.valueParameters.size == 3 }

                            irCall(decodeFunc).apply {
                                dispatchReceiver = irGet(cDecoder)
                                putValueArgument(0, irGet(descriptor))
                                putValueArgument(1, irInt(index))
                                putValueArgument(2, serializer)   // serializer instance

                                putTypeArgument(0, serialProperty.type)
                            }
                        }

                        serialInfo.encodeType != null -> {
                            val decodeFunc =
                                compositeDecoderClass.functions.single { it.name.asString() == "decode${serialInfo.encodeType}Element" }
                            irCall(decodeFunc).apply {
                                dispatchReceiver = irGet(cDecoder)
                                putValueArgument(0, irGet(descriptor))
                                putValueArgument(1, irInt(index))
                            }
                        }

                        else -> error(errorMsg)
                    }

                    val setVar = irSet(localVar, callDecode)

                    irBranch(condition, setVar)
                }.toMutableList()

                // break loop
                branches.add(
                    irBranch(irEquals(irGet(idx), irInt(-1)), irBreak(loop))
                )

                +irWhen(context.irBuiltIns.unitType, branches)
            }
        }

        // cDecoder.endStructure(descriptor)
        val decoderEnd =
            compositeDecoderClass.functions.single { it.name == END_STRUCTURE && it.valueParameters.size == 1 }
        +irCall(decoderEnd).apply {
            dispatchReceiver = irGet(cDecoder)
            putValueArgument(0, irGet(descriptor))
        }

        // check local vars is not null
        val missingPropertyExceptionCtr = pluginContext.referenceConstructors(missingPropertyExceptionClassId).single()
        fun throwPropertyException(propName: String) = irThrow(irCall(missingPropertyExceptionCtr).apply {
            putValueArgument(0, irString(propName))
        })

        serialProperty.properties.forEach { prop ->
            val localVar =
                localVars[prop] ?: error("not found local var for property ${prop.name}")
            +irIfThen(irEqualsNull(irGet(localVar)), throwPropertyException(prop.name.asString()))
        }

        // call constructor
        val serializableClassCtr = serializableClass.primaryConstructor ?: error("not found ${serializableClass.name} primary constructor")
        assert(serializableClassCtr.valueParameters.size == serialProperty.properties.size) {
            "currently only support all properties in data class will be serialized"
        }
        +irReturn(irCall(serializableClassCtr).apply {
            irClass.typeParameters.forEachIndexed { index, irTypeParameter ->
                putTypeArgument(index, irTypeParameter.defaultType)
            }
            serialProperty.properties.forEachIndexed { index, serialProperty ->
                putValueArgument(index, irGet(localVars[serialProperty]!!) )
            }
        })
    }

    fun generateDescriptor(serialDescriptorProperty: IrProperty) {

//        serialDescriptor.backingField!!.initializer =
        val serialDescriptorPropertyBackingField = serialDescriptorProperty.backingField!!


        val pluginGeneratedDescriptorClass = referenceClass(pluginGeneratedDescriptor)

        val addElementFunc =
            pluginGeneratedDescriptorClass.functions.single { it.name.asString() == "element" && it.valueParameters.size == 2 }


        irClass.addAnonymousInit {

            val newDescriptor = irCall(pluginGeneratedDescriptorClass.primaryConstructor!!).apply {
                putValueArgument(0, irString(serializableClass.name.asString()))
                putValueArgument(1, irInt(serialProperty.properties.size))
                putValueArgument(2, irGet(irClass.thisReceiver!!))
            }
            val descriptor = irTemporary(newDescriptor, nameHint = "descriptor")

            serialProperty.properties.forEachIndexed { index, serialProperty ->

                +irCall(addElementFunc).apply {
                    dispatchReceiver = irGet(descriptor)

                    putValueArgument(0, irString(serialProperty.name.asString()))
                    putValueArgument(1, irFalse())
                }
            }

            // setback descriptor
            +irSetField(
                irGet(irClass.thisReceiver!!),
                serialDescriptorPropertyBackingField,
                irGet(descriptor)
            )
        }
    }

    fun generatePrivateDefaultConstructor() {
        irClass.primaryConstructor!!.addFunctionBody {
            // call Any
            +irDelegatingConstructorCall(pluginContext.irBuiltIns.anyClass.owner.primaryConstructor!!)
            +IrInstanceInitializerCallImpl(this.startOffset, this.endOffset, irClass.symbol, irClass.defaultType)
        }
    }

    fun generateTypeParameterConstructor() {
        if (irClass.typeParameters.isEmpty()) return
        val ctr = irClass.constructors.single { it.valueParameters.size == irClass.typeParameters.size }
        val primaryCtr = irClass.primaryConstructor!!
        ctr.addFunctionBody {

            +irDelegatingConstructorCall(primaryCtr).apply {
                // class $serializer$<K,V>
                // TODO type argument?
            }

            assert(ctr.valueParameters.size == irClass.typeParameters.size)
            ctr.valueParameters.forEachIndexed { index, irValueParameter ->
                +irSetField(
                    irGet(irClass.thisReceiver!!), localTypeParameterSerializers[index].backingField!!,
                    irGet(irValueParameter),
                )
            }
        }
    }

    fun generateChildSerializer(function: IrSimpleFunction) = function.addFunctionBody {
        val arrayOf = irArrayOf(serializerClassSymbol.starProjectedType,
            serialProperty.properties.map {
                instantiateSerializer(findSerializerByType(it.type), it.type) { index ->
                    irGetField(
                        irGet(function.dispatchReceiverParameter!!),
                        localTypeParameterSerializers[index].backingField!!
                    )
                }
            }
        )
        +irReturn(arrayOf)
    }


    fun generateTPSerializer(function: IrSimpleFunction) = function.addFunctionBody {
        val arrayOf = irArrayOf(serializerClassSymbol.starProjectedType,
            localTypeParameterSerializers.map { prop ->
                irGetField(irGet(function.dispatchReceiverParameter!!), prop.backingField!!)
            })
        +irReturn(arrayOf)
    }


    fun generate() {

        irClass.findPluginGeneratePropertyByName(DESCRIPTOR)?.let { generateDescriptor(it) }
        irClass.findPluginGenerateFunctionByName(SERIALIZE)?.let { generateSerialize(it) }
        irClass.findPluginGenerateFunctionByName(DESERIALIZE)?.let { generateDeserialize(it) }
        irClass.findPluginGenerateFunctionByName(TYPE_PARAMETER_SERIALIZERS)?.let { generateTPSerializer(it) }
        irClass.findPluginGenerateFunctionByName(CHILD_SERIALIZERS)?.let { generateChildSerializer(it) }

        generatePrivateDefaultConstructor()
        generateTypeParameterConstructor()
    }
}