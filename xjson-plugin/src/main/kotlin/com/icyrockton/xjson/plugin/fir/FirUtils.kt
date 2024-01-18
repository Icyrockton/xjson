package com.icyrockton.xjson.plugin.fir

import com.icyrockton.xjson.plugin.XJsonAnnotationParam
import com.icyrockton.xjson.plugin.XJsonClassId.serializableClassId
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationsByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType

context(FirSession)
fun FirBasedSymbol<*>.getSerializableAnnotation(): FirAnnotation? {
    return resolvedAnnotationsWithArguments.getAnnotationsByClassId(serializableClassId, this@FirSession).firstOrNull()
}

context(FirSession)
fun FirBasedSymbol<*>.hasSerializableAnnotationWithoutArgs() : Boolean {
    return getSerializableAnnotation()?.argumentMapping?.mapping?.isEmpty() ?: false
}

context(FirSession)
val FirBasedSymbol<*>.hasSerializableAnnotation: Boolean
    get() = getSerializableAnnotation() != null

context(FirSession)
fun FirBasedSymbol<*>.getSerializableWithClass(): ConeKotlinType? {
    return getSerializableAnnotation()?.getKClassArgument(XJsonAnnotationParam.WITH)
}

context(FirSession)
val FirBasedSymbol<*>.shouldGenerateSerializer: Boolean
    get() = hasSerializableAnnotationWithoutArgs()