package dev.akexorcist.flipfoldcounter.data

import android.content.Context
import android.content.SharedPreferences
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultAppSettingsRepositoryTest : StringSpec({

    lateinit var editor: SharedPreferences.Editor
    lateinit var sharedPreferences: SharedPreferences
    lateinit var repository: DefaultAppSettingsRepository

    beforeTest {
        editor = mock()
        sharedPreferences = mock()
        val context = mock<Context>()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(sharedPreferences.getBoolean(any(), any())).thenReturn(false)

        repository = DefaultAppSettingsRepository(AppSettingsDataSource(context))
    }

    "shouldShowBeforeUsing is true by default" {
        repository.shouldShowBeforeUsing() shouldBe true
    }

    "shouldShowBeforeUsing is false once the persisted flag is true" {
        whenever(sharedPreferences.getBoolean(any(), any())).thenReturn(true)

        repository.shouldShowBeforeUsing() shouldBe false
    }

    "markAsDoNotShowBeforeUsingAgain writes true to shared preferences" {
        repository.markAsDoNotShowBeforeUsingAgain()

        verify(editor).putBoolean("do_not_show_before_using_again", true)
    }
})
