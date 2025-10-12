package dev.akexorcist.flipfoldcounter.data

import android.content.Context
import androidx.core.content.edit

class AppSettingsDataSource(context: Context) {
    companion object {
        private const val KEY_DO_NOT_SHOW_BEFORE_USING_AGAIN = "do_not_show_before_using_again"
    }

    private val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun setDoNotShowBeforeUsingAgain() {
        sharedPreferences.edit {
            putBoolean(KEY_DO_NOT_SHOW_BEFORE_USING_AGAIN, true)
        }
    }

    fun getDoNotShowBeforeUsingAgain() = sharedPreferences.getBoolean(KEY_DO_NOT_SHOW_BEFORE_USING_AGAIN, false)
}
