package com.ks.tvlauncher.domain

/** Kind of TV input, mapped from `TvInputInfo.TYPE_*` by the data layer. */
enum class InputKind { HDMI, DISPLAY_PORT, DVI, COMPONENT, COMPOSITE, SVIDEO, SCART, VGA, OTHER }

/** Whether something is plugged into an input, from `TvInputManager.getInputState`. */
enum class InputConnection { CONNECTED, STANDBY, DISCONNECTED }

/** One input as the TV reports it, before filtering. */
data class TvInputSource(
    val id: String,
    val kind: InputKind,
    val passthrough: Boolean,
    /** Name the user gave the input in TV settings, if any. */
    val customLabel: String?,
    /** The input's own name, for example "HDMI 1". */
    val label: String?,
    val connection: InputConnection,
)

/** An input KS lists in Control Center. */
data class TvInput(
    val id: String,
    val label: String,
    val kind: InputKind,
    val connection: InputConnection,
)

/** Which inputs Control Center lists, their names and their order. */
object TvInputs {

    /**
     * Passthrough inputs only (HDMI, AV, component; never tuners or app channels), named by
     * [label], ordered by kind (HDMI first) and then by the input id with numbers compared as
     * numbers, so HW2, HW3, HW10 keep the TV's own port order even after a rename.
     */
    fun select(sources: List<TvInputSource>): List<TvInput> =
        sources
            .filter { it.passthrough }
            .distinctBy { it.id }
            .sortedWith(compareBy<TvInputSource> { it.kind.ordinal }.then { a, b -> naturalCompare(a.id, b.id) })
            .map { TvInput(it.id, label(it.customLabel, it.label, it.id), it.kind, it.connection) }

    /** The user's name if set, else the input's own name, else its id. Blank names count as unset. */
    fun label(custom: String?, own: String?, id: String): String =
        custom?.trim()?.takeIf { it.isNotEmpty() } ?: own?.trim()?.takeIf { it.isNotEmpty() } ?: id

    /** Compares strings with digit runs compared by value ("HW2" before "HW10"). */
    internal fun naturalCompare(a: String, b: String): Int {
        var i = 0
        var j = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]
            if (ca.isDigit() && cb.isDigit()) {
                val si = i
                val sj = j
                while (i < a.length && a[i].isDigit()) i++
                while (j < b.length && b[j].isDigit()) j++
                val na = a.substring(si, i).trimStart('0')
                val nb = b.substring(sj, j).trimStart('0')
                if (na.length != nb.length) return na.length - nb.length
                val c = na.compareTo(nb)
                if (c != 0) return c
            } else {
                if (ca != cb) return ca - cb
                i++
                j++
            }
        }
        return (a.length - i) - (b.length - j)
    }
}
