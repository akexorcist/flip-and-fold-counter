package dev.akexorcist.flipfoldcounter.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultAppSettingsRepositoryTest {

    private val editor = mock<SharedPreferences.Editor>()
    private val sharedPreferences = mock<SharedPreferences>()
    private lateinit var repository: DefaultAppSettingsRepository

    @Before
    fun setUp() {
        val context = mock<Context>()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(sharedPreferences.getBoolean(any(), any())).thenReturn(false)

        repository = DefaultAppSettingsRepository(AppSettingsDataSource(context))
    }

    @Test
    fun `shouldShowBeforeUsing is true by default`() {
        assertTrue(repository.shouldShowBeforeUsing())
    }

    @Test
    fun `shouldShowBeforeUsing is false once the persisted flag is true`() {
        whenever(sharedPreferences.getBoolean(any(), any())).thenReturn(true)

        assertFalse(repository.shouldShowBeforeUsing())
    }

    @Test
    fun `markAsDoNotShowBeforeUsingAgain writes true to shared preferences`() {
        repository.markAsDoNotShowBeforeUsingAgain()

        verify(editor).putBoolean("do_not_show_before_using_again", true)
    }
}
