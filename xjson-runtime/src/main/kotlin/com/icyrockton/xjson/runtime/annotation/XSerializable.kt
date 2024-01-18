package com.icyrockton.xjson.runtime.annotation

import com.icyrockton.xjson.runtime.XSerializer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class XSerializable(val with: KClass<out XSerializer<*>> = XSerializer::class)