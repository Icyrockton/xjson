package com.icyrockton.xjson.plugin.fir

import com.icyrockton.xjson.plugin.XJsonClassId.pluginGeneratedSerializerClassId
import com.icyrockton.xjson.plugin.XJsonClassId.serializableFqName
import com.icyrockton.xjson.plugin.XJsonClassId.serializerClassId
import com.icyrockton.xjson.plugin.XJsonNames
import com.icyrockton.xjson.plugin.XJsonNames.TYPE_PARAMETER_SERIALIZER_VALUE_PARAMETER_NAME
import com.icyrockton.xjson.plugin.XJsonPluginGenerated
import com.icyrockton.xjson.plugin.XJsonPluginKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.plugin.*
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.getFunctions
import org.jetbrains.kotlin.fir.scopes.getProperties
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.scopes.scopeForSupertype
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

class FirSerializerGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> = with(session) {
        val names = hashSetOf<Name>()

        if (classSymbol.shouldGenerateSerializer)
            names.add(XJsonPluginGenerated.GenerateSerializer)

        return names
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val classId = classSymbol.classId
        val names = hashSetOf<Name>()

        when {
            classId.shortClassName == XJsonPluginGenerated.GenerateSerializer -> {
                // `$serializer$` class members
                names += listOf(
                    SpecialNames.INIT,
                    XJsonNames.SERIALIZE,
                    XJsonNames.DESERIALIZE,
                    XJsonNames.DESCRIPTOR,
                )

                // if have typeParameter
                if (classSymbol.resolvedSuperTypes.any { it.classId == pluginGeneratedSerializerClassId }) {
                    names += XJsonNames.CHILD_SERIALIZERS
                    names += XJsonNames.TYPE_PARAMETER_SERIALIZERS
                }
            }
        }

        return names
    }


    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if (owner !is FirRegularClassSymbol) return null

        return when (name) {
            XJsonPluginGenerated.GenerateSerializer -> generateSerializerClass(owner)
            else -> error("Can't generate nested class ${owner.classId.createNestedClassId(name)}")
        }
    }

    /**
     * Generate $serializer$ class/object
     */
    private fun generateSerializerClass(owner: FirRegularClassSymbol): FirClassLikeSymbol<*> {
        val kind = if (owner.typeParameterSymbols.isEmpty()) ClassKind.OBJECT else ClassKind.CLASS
        return createNestedClass(owner, XJsonPluginGenerated.GenerateSerializer, XJsonPluginKey, kind) {

            // add type parameter
            owner.typeParameterSymbols.forEach {
                typeParameter(it.name)
            }

            // add supertype
            // class $serializer$<T,V> : PluginGeneratedXSerializer<owner<T,V>>
            superType { typeParameters ->
                pluginGeneratedSerializerClassId.constructClassLikeType(
                    arrayOf(
                        owner.constructType(
                            typeParameters.map { it.toConeType() }.toTypedArray(), false
                        )
                    )
                )
            }
        }.symbol
    }


    private fun <T> getFromSuperTypes(owner: FirClassSymbol<*>, extractor: FirTypeScope.() -> List<T>): T {
        val scopeSession = ScopeSession()
        val scopes =
            lookupSuperTypes(owner, lookupInterfaces = true, deep = false, useSiteSession = session).mapNotNull {
                it.scopeForSupertype(session, scopeSession, owner.fir, null)
            }
        val result = scopes.flatMap { it.extractor() }
        return result.singleOrNull() ?: error("multiple xx symbols found in superTypes")
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        val callableName = callableId.callableName
        if (callableName !in listOf(
                XJsonNames.SERIALIZE,
                XJsonNames.DESERIALIZE,
                XJsonNames.TYPE_PARAMETER_SERIALIZERS,
                XJsonNames.CHILD_SERIALIZERS,
            )
        ) return emptyList()

        val fromSuperSymbol = getFromSuperTypes(owner) { this.getFunctions(callableName) }
        val originFunc = fromSuperSymbol.fir
        // need copy function, because value parameterS
        val newSymbol = buildSimpleFunctionCopy(originFunc) {
            symbol = FirNamedFunctionSymbol(callableId)
            status = originFunc.status.copy(modality = Modality.FINAL)
            origin = XJsonPluginKey.origin
        }.symbol

        return listOf(newSymbol)
    }


    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        val callableName = callableId.callableName
        if (callableName != XJsonNames.DESCRIPTOR) return emptyList()
        val fromSuperSymbol = getFromSuperTypes(owner) { this.getProperties(callableName) }

        val property = createMemberProperty(owner, XJsonPluginKey, callableName, fromSuperSymbol.resolvedReturnType)

        return listOf(property.symbol)
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner = context.owner
        val constructors = mutableListOf<FirConstructorSymbol>()
        constructors += createDefaultPrivateConstructor(
            owner,
            XJsonPluginKey,
            generateDelegatedNoArgConstructorCall = false
        ).symbol

        // if serializer has type parameters, we create another constructor with value parameters (XSerializers)
        if (owner.classId.shortClassName == XJsonPluginGenerated.GenerateSerializer
            && owner.typeParameterSymbols.isNotEmpty()
        ) {
            constructors += createConstructor(
                owner,
                XJsonPluginKey,
                isPrimary = false,
                generateDelegatedNoArgConstructorCall = false
            ) {
                visibility = Visibilities.Public
                owner.typeParameterSymbols.forEachIndexed { index, parameterSymbol ->
                    val tName = TYPE_PARAMETER_SERIALIZER_VALUE_PARAMETER_NAME(index)
                    val tType = serializerClassId.constructClassLikeType(
                        arrayOf(parameterSymbol.toConeType()),
                        isNullable = false
                    )
                    valueParameter(tName, tType)
                }
            }.symbol
        }

        return constructors
    }

    /**
     * NB: The predict needs to be *registered* in order to parse the [@XSerializable] type
     * otherwise, the annotation remains unresolved
     */
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        val serializable = LookupPredicate.create { annotated(serializableFqName) }
        register(serializable)
    }
}