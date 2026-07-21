package dev.akexorcist.flipfoldcounter.data

class FakeAppSettingsRepository(
    initialShouldShow: Boolean = true,
) : AppSettingsRepository {
    private var doNotShowAgain = !initialShouldShow

    override fun markAsDoNotShowBeforeUsingAgain() {
        doNotShowAgain = true
    }

    override fun shouldShowBeforeUsing(): Boolean = !doNotShowAgain
}
