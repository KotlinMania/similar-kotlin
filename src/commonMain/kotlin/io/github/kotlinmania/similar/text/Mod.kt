// port-lint: source text/mod.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.Algorithm
import io.github.kotlinmania.similar.Change
import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.captureDiffSlicesDeadline
import io.github.kotlinmania.similar.getDiffRatio
import io.github.kotlinmania.similar.groupDiffOps
import io.github.kotlinmania.similar.UnifiedDiff
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.toDuration

internal sealed class Deadline {
    data class Absolute(val instant: TimeMark) : Deadline()
    data class Relative(val duration: Duration) : Deadline()

    fun intoInstant(): TimeMark =
        when (this) {
            is Absolute -> instant
            is Relative -> TimeSource.Monotonic.markNow() + duration
        }
}

/** A builder type config for more complex uses of [TextDiff]. */
class TextDiffConfig internal constructor(
    private var algorithmValue: Algorithm = Algorithm.Myers,
    private var newlineTerminatedValue: Boolean? = null,
    private var deadlineValue: Deadline? = null,
) {
    /** Changes the algorithm. */
    fun algorithm(alg: Algorithm): TextDiffConfig {
        algorithmValue = alg
        return this
    }

    /** Sets a deadline for the diff operation. */
    fun deadline(deadline: TimeMark): TextDiffConfig {
        deadlineValue = Deadline.Absolute(deadline)
        return this
    }

    /** Sets a timeout for the diff operation. */
    fun timeout(timeout: Duration): TextDiffConfig {
        deadlineValue = Deadline.Relative(timeout)
        return this
    }

    /** Changes the newline termination flag. */
    fun newlineTerminated(yes: Boolean): TextDiffConfig {
        newlineTerminatedValue = yes
        return this
    }

    /** Creates a diff of lines. */
    fun diffLines(old: String, new: String): TextDiff =
        diff(old.asDiffableStr().tokenizeLines(), new.asDiffableStr().tokenizeLines(), true)

    /** Creates a diff of words. */
    fun diffWords(old: String, new: String): TextDiff =
        diff(old.asDiffableStr().tokenizeWords(), new.asDiffableStr().tokenizeWords(), false)

    /** Creates a diff of characters. */
    fun diffChars(old: String, new: String): TextDiff =
        diff(old.asDiffableStr().tokenizeChars(), new.asDiffableStr().tokenizeChars(), false)

    /** Creates a diff of unicode words. */
    fun diffUnicodeWords(old: String, new: String): TextDiff =
        diff(old.asDiffableStr().tokenizeUnicodeWords(), new.asDiffableStr().tokenizeUnicodeWords(), false)

    /** Creates a diff of graphemes. */
    fun diffGraphemes(old: String, new: String): TextDiff =
        diff(old.asDiffableStr().tokenizeGraphemes(), new.asDiffableStr().tokenizeGraphemes(), false)

    /** Creates a diff of arbitrary slices. */
    fun diffSlices(old: List<String>, new: List<String>): TextDiff =
        diff(old, new, false)

    private fun diff(old: List<String>, new: List<String>, newlineTerminated: Boolean): TextDiff {
        val deadline = deadlineValue?.intoInstant()
        val ops = captureDiffSlicesDeadline(algorithmValue, old, new, deadline)
        return TextDiff(
            old = old,
            new = new,
            ops = ops,
            newlineTerminated = newlineTerminatedValue ?: newlineTerminated,
            algorithm = algorithmValue,
        )
    }
}

/**
 * Captures diff op codes for textual diffs.
 *
 * The exact diff behavior is depending on the underlying [DiffableStr].
 * You can create a text diff from constructors such as [fromLines] or the
 * [TextDiffConfig] created by [configure].
 */
data class TextDiff(
    private val old: List<String>,
    private val new: List<String>,
    private val ops: List<DiffOp>,
    private val newlineTerminated: Boolean,
    private val algorithm: Algorithm,
) {
    companion object {
        /** Configures a text differ before diffing. */
        fun configure(): TextDiffConfig = TextDiffConfig()

        /** Creates a diff of lines. */
        fun fromLines(old: String, new: String): TextDiff = configure().diffLines(old, new)

        /** Creates a diff of words. */
        fun fromWords(old: String, new: String): TextDiff = configure().diffWords(old, new)

        /** Creates a diff of chars. */
        fun fromChars(old: String, new: String): TextDiff = configure().diffChars(old, new)

        /** Creates a diff of unicode words. */
        fun fromUnicodeWords(old: String, new: String): TextDiff = configure().diffUnicodeWords(old, new)

        /** Creates a diff of graphemes. */
        fun fromGraphemes(old: String, new: String): TextDiff = configure().diffGraphemes(old, new)

        /** Creates a diff of arbitrary slices. */
        fun fromSlices(old: List<String>, new: List<String>): TextDiff = configure().diffSlices(old, new)
    }

    /** The name of the algorithm that created the diff. */
    fun algorithm(): Algorithm = algorithm

    /** Returns `true` if items in the slice are newline terminated. */
    fun newlineTerminated(): Boolean = newlineTerminated

    /** Returns all old slices. */
    fun oldSlices(): List<String> = old

    /** Returns all new slices. */
    fun newSlices(): List<String> = new

    /** Return a measure of the sequences' similarity in the range from `0.0` through `1.0`. */
    fun ratio(): Float = getDiffRatio(ops(), old.size, new.size)

    /** Iterates over the changes the op expands to. */
    fun iterChanges(op: DiffOp): Iterator<Change<String>> =
        op.iterChanges(old.asLookup(), new.asLookup())

    /** Returns the captured diff ops. */
    fun ops(): List<DiffOp> = ops

    /** Isolate change clusters by eliminating ranges with no changes. */
    fun groupedOps(n: Int): List<List<DiffOp>> = groupDiffOps(ops().toList(), n)

    /** Flattens out the diff into all changes. */
    fun iterAllChanges(): Iterator<Change<String>> = io.github.kotlinmania.similar.AllChangesIter(old, new, ops)

    /** Utility to return a unified diff formatter. */
    fun unifiedDiff(): UnifiedDiff = UnifiedDiff.fromTextDiff(this)

    /** Iterates over the changes the op expands to with inline emphasis. */
    fun iterInlineChanges(op: DiffOp): Iterator<InlineChange> =
        iterInlineChangesDeadline(op, TimeSource.Monotonic.markNow() + 500.milliseconds)

    /** Iterates over the changes the op expands to with inline emphasis with a deadline. */
    fun iterInlineChangesDeadline(op: DiffOp, deadline: TimeMark?): Iterator<InlineChange> =
        iterInlineChanges(this, op, deadline)
}

/** Use the text differ to find `n` close matches. */
fun getCloseMatches(word: String, possibilities: List<String>, n: Int, cutoff: Float): List<String> {
    val matches = mutableListOf<Pair<Float, String>>()
    val seq1 = word.asDiffableStr().tokenizeChars()
    val quickRatio = QuickSeqRatio(seq1)

    for (possibility in possibilities) {
        val seq2 = possibility.asDiffableStr().tokenizeChars()
        if (upperSeqRatio(seq1, seq2) < cutoff || quickRatio.calc(seq2) < cutoff) {
            continue
        }

        val diff = TextDiff.fromSlices(seq1, seq2)
        val ratio = diff.ratio()
        if (ratio >= cutoff) {
            matches += ratio to possibility
        }
    }

    return matches
        .sortedWith(compareByDescending<Pair<Float, String>> { it.first }.thenBy { it.second })
        .take(n)
        .map { it.second }
}

private fun List<String>.asLookup(): io.github.kotlinmania.similar.IndexLookup<String> =
    io.github.kotlinmania.similar.IndexLookup { this[it] }

private val Int.milliseconds: Duration
    get() = toDuration(DurationUnit.MILLISECONDS)
