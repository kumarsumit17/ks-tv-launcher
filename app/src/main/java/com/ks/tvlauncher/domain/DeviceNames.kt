package com.ks.tvlauncher.domain

object DeviceNames {
    /**
     * A readable model name from `Build.MANUFACTURER` and `Build.MODEL`: the manufacturer is
     * capitalised and not repeated when the model already starts with it.
     * ("google", "Chromecast") → "Google Chromecast"; ("Sony", "SONY BRAVIA 4K") → "SONY BRAVIA 4K".
     */
    fun tidyModel(manufacturer: String?, model: String?): String {
        val maker = manufacturer.orEmpty().trim().takeUnless { it.equals("unknown", ignoreCase = true) }.orEmpty()
        val name = model.orEmpty().trim()
        return when {
            maker.isEmpty() -> name.capitalised()
            name.isEmpty() -> maker.capitalised()
            name.startsWith(maker, ignoreCase = true) -> name.capitalised()
            else -> "${maker.capitalised()} $name"
        }
    }

    /**
     * The TV name to show: the user's own name when that option is on and filled in, otherwise
     * the name from Android Settings › Device name, otherwise the tidied model.
     */
    fun displayName(useOwn: Boolean, ownName: String, systemName: String?, model: String): String {
        val own = ownName.trim()
        if (useOwn && own.isNotEmpty()) return own
        return systemName?.trim()?.takeIf { it.isNotEmpty() } ?: model
    }

    private fun String.capitalised(): String = replaceFirstChar { it.uppercaseChar() }
}
