package com.icyrockton.xjson.plugin.runners

import com.icyrockton.xjson.plugin.services.XJsonEnvironmentConfigurator
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.ir.JvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureFirHandlersStep
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.frontend.fir.Fir2IrResultsConverter
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.ArtifactKinds
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.runners.codegen.commonConfigurationForTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JvmEnvironmentConfigurator
import org.junit.jupiter.api.BeforeAll

abstract class AbstractTestRunner : AbstractKotlinCompilerTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration() // set system property to initialize idea service
        }
    }

    override fun TestConfigurationBuilder.configuration() {
        globalDefaults {
            frontend = FrontendKinds.FIR
            targetBackend = TargetBackend.JVM_IR
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            artifactKind = ArtifactKinds.Jvm
            dependencyKind = DependencyKind.Source
            languageSettings {
                languageVersion = LanguageVersion.KOTLIN_2_1
                apiVersion = ApiVersion.KOTLIN_2_1
            }
        }

        defaultDirectives {
            FIR_PARSER with FirParser.LightTree
        }

        commonConfigurationForTest(
            FrontendKinds.FIR,
            ::FirFrontendFacade,
            ::Fir2IrResultsConverter,
            ::JvmIrBackendFacade
        ) { }

        configureHandlers()
        configureFirHandlersStep {

        }

        configureJvmArtifactsHandlersStep {

        }

        useConfigurators(
            ::CommonEnvironmentConfigurator,     // compiler flags
            ::JvmEnvironmentConfigurator,        // jdk and kotlin runtime configuration (e.g. FULL_JDK)
            ::XJsonEnvironmentConfigurator,    // compiler plugin configuration
        )

    }

    abstract fun TestConfigurationBuilder.configureHandlers()

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}