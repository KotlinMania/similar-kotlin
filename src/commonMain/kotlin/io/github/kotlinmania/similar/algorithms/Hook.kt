// port-lint: source src/algorithms/hook.rs
package io.github.kotlinmania.similar.algorithms

/**
 * Result of a [DiffHook] call.
 *
 * In Kotlin this stands in for the upstream `Result<(), Self::Error>` — the
 * hook either succeeds (with no payload) or carries an implementation-defined
 * error of type [E].
 */
sealed class DiffHookResult<out E> {
    /** Successful hook call. */
    object Ok : DiffHookResult<Nothing>()

    /** Failed hook call carrying the implementation-defined error. */
    class Err<out E>(val error: E) : DiffHookResult<E>()
}

/**
 * A trait for reacting to an edit script from the "old" version to
 * the "new" version.
 */
interface DiffHook<E> {
    /**
     * Called when lines with indices `oldIndex` (in the old version) and
     * `newIndex` (in the new version) start an section equal in both
     * versions, of length `len`.
     */
    fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<E> {
        return DiffHookResult.Ok
    }

    /**
     * Called when a section of length `oldLen`, starting at `oldIndex`,
     * needs to be deleted from the old version.
     */
    fun delete(oldIndex: Int, oldLen: Int, newIndex: Int): DiffHookResult<E> {
        return DiffHookResult.Ok
    }

    /**
     * Called when a section of the new version, of length `newLen`
     * and starting at `newIndex`, needs to be inserted at position `oldIndex`.
     */
    fun insert(oldIndex: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        return DiffHookResult.Ok
    }

    /**
     * Called when a section of the old version, starting at index
     * `oldIndex` and of length `oldLen`, needs to be replaced with a
     * section of length `newLen`, starting at `newIndex`, of the new
     * version.
     *
     * The default implementations invokes [delete] and [insert].
     *
     * You can use the [Replace] hook to automatically generate these.
     */
    fun replace(oldIndex: Int, oldLen: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        return when (val r = delete(oldIndex, oldLen, newIndex)) {
            is DiffHookResult.Err -> r
            is DiffHookResult.Ok -> insert(oldIndex, newIndex, newLen)
        }
    }

    /** Always called at the end of the algorithm. */
    fun finish(): DiffHookResult<E> {
        return DiffHookResult.Ok
    }
}

/**
 * Wrapper [DiffHook] that prevents calls to [DiffHook.finish].
 *
 * This hook is useful in situations where diff hooks are composed but you
 * want to prevent that the finish hook method is called.
 */
class NoFinishHook<E, D : DiffHook<E>>(private val inner: D) : DiffHook<E> {
    /** Extracts the inner hook. */
    fun intoInner(): D = inner

    override fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<E> {
        return inner.equal(oldIndex, newIndex, len)
    }

    override fun delete(oldIndex: Int, oldLen: Int, newIndex: Int): DiffHookResult<E> {
        return inner.delete(oldIndex, oldLen, newIndex)
    }

    override fun insert(oldIndex: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        return inner.insert(oldIndex, newIndex, newLen)
    }

    override fun replace(oldIndex: Int, oldLen: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        return inner.replace(oldIndex, oldLen, newIndex, newLen)
    }

    override fun finish(): DiffHookResult<E> {
        return DiffHookResult.Ok
    }
}
