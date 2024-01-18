package com.icyrockton.xjson.plugin.runners

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureFirHandlersStep
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirCfgDumpHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDiagnosticsHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDumpHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirResolvedTypesVerifier

abstract class AbstractFirTestRunner : AbstractTestRunner() {
    override fun TestConfigurationBuilder.configureHandlers() {
        configureFirHandlersStep {
            useHandlers(::FirDumpHandler, ::FirCfgDumpHandler, ::FirResolvedTypesVerifier, ::FirDiagnosticsHandler)
        }
    }
}