// port-lint: source algorithms/replace.rs
package io.github.kotlinmania.similar.algorithms

/**
 * A [DiffHook] that combines deletions and insertions to give blocks
 * of maximal length, and replacements when appropriate.
 *
 * It will replace [DiffHook.insert] and [DiffHook.delete] events when
 * possible with [DiffHook.replace] events. Note that even though the
 * text processing in the crate does not use replace events and always resolves
 * them back to delete and insert, it is useful to always use the replacer to
 * ensure a consistent order of inserts and deletes. This is why for instance
 * the text diffing automatically uses this hook internally.
 */
class Replace<E, D : DiffHook<E>> private constructor(
    private val d: D,
    private var del: Triple<Int, Int, Int>?,
    private var ins: Triple<Int, Int, Int>?,
    private var eq: Triple<Int, Int, Int>?,
) : DiffHook<E> {
    constructor(d: D) : this(d, null, null, null)

    /** Extracts the inner hook. */
    fun intoInner(): D = d

    private fun flushEq(): DiffHookResult<E> {
        val current = eq
        if (current != null) {
            eq = null
            return d.equal(current.first, current.second, current.third)
        }
        return DiffHookResult.Ok
    }

    private fun flushDelIns(): DiffHookResult<E> {
        val currentDel = del
        if (currentDel != null) {
            del = null
            val currentIns = ins
            return if (currentIns != null) {
                ins = null
                d.replace(currentDel.first, currentDel.second, currentIns.second, currentIns.third)
            } else {
                d.delete(currentDel.first, currentDel.second, currentDel.third)
            }
        }

        val currentIns = ins
        if (currentIns != null) {
            ins = null
            return d.insert(currentIns.first, currentIns.second, currentIns.third)
        }

        return DiffHookResult.Ok
    }

    /** Returns the wrapped hook. */
    fun asRef(): D = d

    /** Returns the wrapped hook. */
    fun asMut(): D = d

    override fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<E> {
        when (val flushed = flushDelIns()) {
            is DiffHookResult.Err -> return flushed
            is DiffHookResult.Ok -> {}
        }

        val current = eq
        eq = if (current != null) {
            Triple(current.first, current.second, current.third + len)
        } else {
            Triple(oldIndex, newIndex, len)
        }

        return DiffHookResult.Ok
    }

    override fun delete(oldIndex: Int, oldLen: Int, newIndex: Int): DiffHookResult<E> {
        when (val flushed = flushEq()) {
            is DiffHookResult.Err -> return flushed
            is DiffHookResult.Ok -> {}
        }

        val current = del
        del = if (current != null) {
            check(oldIndex == current.first + current.second)
            Triple(current.first, current.second + oldLen, current.third)
        } else {
            Triple(oldIndex, oldLen, newIndex)
        }

        return DiffHookResult.Ok
    }

    override fun insert(oldIndex: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        when (val flushed = flushEq()) {
            is DiffHookResult.Err -> return flushed
            is DiffHookResult.Ok -> {}
        }

        val current = ins
        ins = if (current != null) {
            check(current.second + current.third == newIndex)
            Triple(current.first, current.second, newLen + current.third)
        } else {
            Triple(oldIndex, newIndex, newLen)
        }

        return DiffHookResult.Ok
    }

    override fun replace(oldIndex: Int, oldLen: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        when (val flushed = flushEq()) {
            is DiffHookResult.Err -> return flushed
            is DiffHookResult.Ok -> {}
        }
        return d.replace(oldIndex, oldLen, newIndex, newLen)
    }

    override fun finish(): DiffHookResult<E> {
        when (val flushed = flushEq()) {
            is DiffHookResult.Err -> return flushed
            is DiffHookResult.Ok -> {}
        }
        return when (val flushed = flushDelIns()) {
            is DiffHookResult.Err -> flushed
            is DiffHookResult.Ok -> d.finish()
        }
    }
}
