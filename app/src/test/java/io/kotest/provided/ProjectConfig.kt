package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// viewModelScope resolves Dispatchers.Main, which isn't available on the JVM unit test
// classpath. UnconfinedTestDispatcher runs launched coroutines eagerly, so ViewModel tests
// can assert on state right after calling an action, without manual scheduler advancing.
@OptIn(ExperimentalCoroutinesApi::class)
object MainDispatcherListener : BeforeTestListener, AfterTestListener {
    override suspend fun beforeTest(testCase: TestCase) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        Dispatchers.resetMain()
    }
}

object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(MainDispatcherListener)
}
