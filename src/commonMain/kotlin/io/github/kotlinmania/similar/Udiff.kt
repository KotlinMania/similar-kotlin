// port-lint: source udiff.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.text.TextDiff

private class MissingNewlineHint(private val enabled: Boolean) {
    override fun toString(): String =
        if (enabled) {
            "\n\\ No newline at end of file"
        } else {
            ""
        }
}

private data class UnifiedDiffHunkRange(
    private val start: Int,
    private val end: Int,
) {
    fun start(): Int = start

    fun end(): Int = end

    override fun toString(): String {
        var beginning = start() + 1
        val len = (end() - start()).coerceAtLeast(0)
        return if (len == 1) {
            beginning.toString()
        } else {
            if (len == 0) {
                beginning -= 1
            }
            "$beginning,$len"
        }
    }
}

/** Unified diff hunk header formatter. */
class UnifiedHunkHeader private constructor(
    private val oldRange: UnifiedDiffHunkRange,
    private val newRange: UnifiedDiffHunkRange,
) {
    companion object {
        /** Creates a hunk header from a non-empty list of diff operations. */
        fun new(ops: List<DiffOp>): UnifiedHunkHeader {
            val first = ops.first()
            val last = ops.last()
            return UnifiedHunkHeader(
                oldRange = UnifiedDiffHunkRange(first.oldRange().first, last.oldRange().exclusiveEnd()),
                newRange = UnifiedDiffHunkRange(first.newRange().first, last.newRange().exclusiveEnd()),
            )
        }
    }

    override fun toString(): String = "@@ -$oldRange +$newRange @@"
}

/** Unified diff formatter. */
class UnifiedDiff private constructor(
    private val diff: TextDiff,
) {
    private var contextRadiusValue: Int = 3
    private var missingNewlineHintValue: Boolean = true
    private var headerValue: Pair<String, String>? = null

    companion object {
        /** Creates a formatter from a text diff object. */
        fun fromTextDiff(diff: TextDiff): UnifiedDiff = UnifiedDiff(diff)
    }

    /** Changes the number of context lines between emitted changes. */
    fun contextRadius(n: Int): UnifiedDiff {
        contextRadiusValue = n
        return this
    }

    /** Sets the old and new file labels printed at the top of a non-empty diff. */
    fun header(a: String, b: String): UnifiedDiff {
        headerValue = a to b
        return this
    }

    /** Controls the missing-newline marker. */
    fun missingNewlineHint(yes: Boolean): UnifiedDiff {
        missingNewlineHintValue = yes
        return this
    }

    /** Iterates over all configured hunks. */
    fun iterHunks(): List<UnifiedDiffHunk> =
        diff.groupedOps(contextRadiusValue)
            .filter { it.isNotEmpty() }
            .map { UnifiedDiffHunk.new(it, diff, missingNewlineHintValue) }

    /** Writes the unified diff to an appendable sink. */
    fun toWriter(w: Appendable): Appendable {
        var header = headerValue
        for (hunk in iterHunks()) {
            val value = header
            if (value != null) {
                w.append("--- ")
                w.append(value.first)
                w.append('\n')
                w.append("+++ ")
                w.append(value.second)
                w.append('\n')
                header = null
            }
            hunk.toWriter(w)
        }
        return w
    }

    internal fun headerOpt(header: Pair<String, String>?): UnifiedDiff {
        if (header != null) {
            header(header.first, header.second)
        }
        return this
    }

    override fun toString(): String =
        toWriter(StringBuilder()).toString()
}

/** Unified diff hunk formatter. */
data class UnifiedDiffHunk(
    private val opsValue: List<DiffOp>,
    private val diff: TextDiff,
    private val missingNewlineHintValue: Boolean,
) {
    companion object {
        /** Creates a new hunk for some operations. */
        fun new(
            ops: List<DiffOp>,
            diff: TextDiff,
            missingNewlineHint: Boolean,
        ): UnifiedDiffHunk = UnifiedDiffHunk(ops, diff, missingNewlineHint)
    }

    /** Returns the header for the hunk. */
    fun header(): UnifiedHunkHeader = UnifiedHunkHeader.new(opsValue)

    /** Returns all operations in the hunk. */
    fun ops(): List<DiffOp> = opsValue

    /** Returns the value of the missing-newline flag. */
    fun missingNewlineHint(): Boolean = missingNewlineHintValue

    /** Iterates over all changes in a hunk. */
    fun iterChanges(): Iterator<Change<String>> =
        AllChangesIter(diff.oldSlices(), diff.newSlices(), opsValue)

    /** Writes the hunk to an appendable sink. */
    fun toWriter(w: Appendable): Appendable {
        var idx = 0
        val changes = iterChanges()
        while (changes.hasNext()) {
            val change = changes.next()
            if (idx == 0) {
                w.append(header().toString())
                w.append('\n')
            }
            w.append(change.tag().toString())
            w.append(change.toStringLossy())
            if (!diff.newlineTerminated()) {
                w.append('\n')
            }
            if (diff.newlineTerminated() && change.missingNewline()) {
                w.append(MissingNewlineHint(missingNewlineHintValue).toString())
                w.append('\n')
            }
            idx += 1
        }
        return w
    }

    override fun toString(): String =
        toWriter(StringBuilder()).toString()
}

/** Quick way to get a unified diff as string. */
fun unifiedDiff(
    alg: Algorithm,
    old: String,
    new: String,
    n: Int,
    header: Pair<String, String>? = null,
): String {
    val formatter = TextDiff.configure()
        .algorithm(alg)
        .diffLines(old, new)
        .unifiedDiff()
        .contextRadius(n)
        .headerOpt(header)
    return formatter.toString()
}

private fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }
