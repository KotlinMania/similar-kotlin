// port-lint: source text/abstraction.rs
package io.github.kotlinmania.similar.text

/**
 * Reference to a [DiffableStr].
 *
 * This type exists because while the library only really provides ways to
 * work with strings, there are types that dereference into those string
 * slices such as `String`.
 *
 * This interface is used in the library whenever it is nice to be able to pass
 * strings of different types in.
 */
interface DiffableStrRef {
    /** Resolves the reference. */
    fun asDiffableStr(): DiffableStr
}

/**
 * All supported diffable strings.
 *
 * The text module can work with different types of strings depending on how
 * the crate is compiled. Out of the box `String` is always supported.
 */
interface DiffableStr : DiffableStrRef {
    /** Splits the value into newlines with newlines attached. */
    fun tokenizeLines(): List<String>

    /** Splits the value into newlines with newlines separated. */
    fun tokenizeLinesAndNewlines(): List<String>

    /** Tokenizes into words. */
    fun tokenizeWords(): List<String>

    /** Tokenizes the input into characters. */
    fun tokenizeChars(): List<String>

    /** Tokenizes into unicode words. */
    fun tokenizeUnicodeWords(): List<String>

    /** Tokenizes into unicode graphemes. */
    fun tokenizeGraphemes(): List<String>

    /** Decodes the string if it is UTF-8. */
    fun asStr(): String?

    /** Decodes the string lossily as UTF-8 string. */
    fun toStringLossy(): String

    /** Checks if the string ends in a newline. */
    fun endsWithNewline(): Boolean

    /** The length of the string. */
    fun len(): Int

    /** Slices the string. */
    fun slice(range: IntRange): String

    /** Returns the string as a slice of raw bytes. */
    fun asBytes(): ByteArray

    /** Checks if the string is empty. */
    fun isEmpty(): Boolean = len() == 0

    override fun asDiffableStr(): DiffableStr = this
}

/** Diffable string wrapper for Kotlin [String] values. */
data class DiffableString(private val value: String) : DiffableStr {
    override fun tokenizeLines(): List<String> {
        val iter = value.withIndex().iterator()
        var lastPos = 0
        val lines = mutableListOf<String>()

        while (iter.hasNext()) {
            val (idx, c) = iter.next()
            if (c == '\r') {
                if (idx + 1 < value.length && value[idx + 1] == '\n') {
                    lines += value.substring(lastPos, idx + 2)
                    if (iter.hasNext()) {
                        iter.next()
                    }
                    lastPos = idx + 2
                } else {
                    lines += value.substring(lastPos, idx + 1)
                    lastPos = idx + 1
                }
            } else if (c == '\n') {
                lines += value.substring(lastPos, idx + 1)
                lastPos = idx + 1
            }
        }

        if (lastPos < value.length) {
            lines += value.substring(lastPos)
        }

        return lines
    }

    override fun tokenizeLinesAndNewlines(): List<String> {
        val rv = mutableListOf<String>()
        var idx = 0
        while (idx < value.length) {
            val isNewline = value[idx] == '\r' || value[idx] == '\n'
            val start = idx
            idx += 1
            while (idx < value.length && ((value[idx] == '\r' || value[idx] == '\n') == isNewline)) {
                idx += 1
            }
            rv += value.substring(start, idx)
        }
        return rv
    }

    override fun tokenizeWords(): List<String> {
        val rv = mutableListOf<String>()
        var idx = 0
        while (idx < value.length) {
            val isWhitespace = value[idx].isWhitespace()
            val start = idx
            idx += 1
            while (idx < value.length && value[idx].isWhitespace() == isWhitespace) {
                idx += 1
            }
            rv += value.substring(start, idx)
        }
        return rv
    }

    override fun tokenizeChars(): List<String> {
        val rv = mutableListOf<String>()
        var index = 0
        while (index < value.length) {
            rv += value.substring(index, index + 1)
            index += 1
        }
        return rv
    }

    override fun tokenizeUnicodeWords(): List<String> = tokenizeWords()

    override fun tokenizeGraphemes(): List<String> = tokenizeChars()

    override fun asStr(): String = value

    override fun toStringLossy(): String = value

    override fun endsWithNewline(): Boolean = value.endsWith('\r') || value.endsWith('\n')

    override fun len(): Int = value.length

    override fun slice(range: IntRange): String = value.substring(range.first, range.exclusiveEnd())

    override fun asBytes(): ByteArray = value.encodeToByteArray()

    override fun toString(): String = value
}

/** Resolves this string as a diffable string. */
fun String.asDiffableStr(): DiffableStr = DiffableString(this)

private fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }
