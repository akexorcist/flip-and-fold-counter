package dev.akexorcist.flipfoldcounter

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.akexorcist.flipfoldcounter.data.db.CounterDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.NumberFormat

/**
 * Simulates Samsung "Modes and Routines" firing while the app isn't open: [CounterActivity] has
 * no intent-filter, so Routines starts it by explicit component name from a separate task, exactly
 * like [triggerCounterActivity] does here. The user then opens the app and
 * should see the total count reflect the trigger.
 */
@RunWith(AndroidJUnit4::class)
class CounterActivityTriggerTest : KoinComponent {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val counterDao: CounterDao by inject()

    @Before
    fun clearExistingCounts() = runBlocking {
        counterDao.clearAll()
    }

    @Test
    fun counterActivity_launchedExternally_incrementsTotalCountByOne() {
        triggerCounterActivity()

        openAppAndAssertDisplayedTotalCount(expected = 1)
    }

    @Test
    fun counterActivity_launchedTwiceInSameHour_incrementsByTwo() {
        triggerCounterActivity()
        triggerCounterActivity()

        openAppAndAssertDisplayedTotalCount(expected = 2)
    }

    private fun triggerCounterActivity() {
        val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val intent = Intent().setClassName(targetPackage, CounterActivity::class.java.name)

        val scenario = ActivityScenario.launch<CounterActivity>(intent)
        val deadline = System.currentTimeMillis() + 5_000
        while (scenario.state != Lifecycle.State.DESTROYED && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        check(scenario.state == Lifecycle.State.DESTROYED) {
            "CounterActivity did not finish (addCounter() + finish()) within timeout"
        }
        scenario.close()
    }

    private fun openAppAndAssertDisplayedTotalCount(expected: Int) {
        val expectedText = NumberFormat.getInstance().format(expected)
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                runCatching {
                    composeTestRule.onNodeWithTag("main_total_count", useUnmergedTree = true)
                        .fetchSemanticsText() == expectedText
                }.getOrDefault(false)
            }
            composeTestRule.onNodeWithTag("main_total_count", useUnmergedTree = true).assertTextEquals(expectedText)
        }
    }

    private fun SemanticsNodeInteraction.fetchSemanticsText(): String =
        fetchSemanticsNode().config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
}
