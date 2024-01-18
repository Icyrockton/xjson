package com.icyrockton.xjson.plugin
import com.icyrockton.xjson.plugin.runners.AbstractFirTestRunner
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5


fun main() {
    println("generating test class...")
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testsRoot = "src/test-gen", testDataRoot = "src/test-data") {
            testClass<AbstractFirTestRunner> {
                model("fir")
            }

        }
    }
}