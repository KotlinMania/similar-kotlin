// port-lint: source algorithms/capture.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.groupDiffOps

/** A [DiffHook] that captures all diff operations. */
class Capture private constructor(
    private val innerOps: MutableList<DiffOp>,
) : DiffHook<Nothing> {
    constructor() : this(mutableListOf())

    companion object {
        /** Creates a new capture hook. */
        fun new(): Capture = Capture()
    }

    /** Converts the capture hook into a list of ops. */
    fun intoOps(): List<DiffOp> = innerOps.toList()

    /**
     * Isolate change clusters by eliminating ranges with no changes.
     *
     * This is equivalent to calling [groupDiffOps] on [intoOps].
     */
    fun intoGroupedOps(n: Int): List<List<DiffOp>> = groupDiffOps(intoOps(), n)

    /** Accesses the captured operations. */
    fun ops(): List<DiffOp> = innerOps.toList()

    override fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<Nothing> {
        innerOps += DiffOp.Equal(
            oldIndex = oldIndex,
            newIndex = newIndex,
            len = len,
        )
        return DiffHookResult.Ok
    }

    override fun delete(oldIndex: Int, oldLen: Int, newIndex: Int): DiffHookResult<Nothing> {
        innerOps += DiffOp.Delete(
            oldIndex = oldIndex,
            oldLen = oldLen,
            newIndex = newIndex,
        )
        return DiffHookResult.Ok
    }

    override fun insert(oldIndex: Int, newIndex: Int, newLen: Int): DiffHookResult<Nothing> {
        innerOps += DiffOp.Insert(
            oldIndex = oldIndex,
            newIndex = newIndex,
            newLen = newLen,
        )
        return DiffHookResult.Ok
    }

    override fun replace(oldIndex: Int, oldLen: Int, newIndex: Int, newLen: Int): DiffHookResult<Nothing> {
        innerOps += DiffOp.Replace(
            oldIndex = oldIndex,
            oldLen = oldLen,
            newIndex = newIndex,
            newLen = newLen,
        )
        return DiffHookResult.Ok
    }
}
