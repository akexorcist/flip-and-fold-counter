package dev.akexorcist.flipfoldcounter.data


interface AppSettingsRepository {
    fun markAsDoNotShowBeforeUsingAgain()
    fun shouldShowBeforeUsing(): Boolean
}

class DefaultAppSettingsRepository(
    private val dataSource: AppSettingsDataSource
) : AppSettingsRepository {
    override fun markAsDoNotShowBeforeUsingAgain() {
        dataSource.setDoNotShowBeforeUsingAgain()
    }

    override fun shouldShowBeforeUsing(): Boolean = !dataSource.getDoNotShowBeforeUsingAgain()
}
