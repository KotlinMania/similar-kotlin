// port-lint: source algorithms/compact.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.DiffTag
import io.github.kotlinmania.similar.IndexLookup

/**
 * Performs semantic cleanup operations on a diff.
 *
 * This merges similar ops together but also tries to move hunks up and
 * down the diff with the desire to connect as many hunks as possible.
 * It still needs to be combined with [Replace] to get actual replace diff
 * ops out.
 */
class Compact<E, T, D : DiffHook<E>>(
    private val d: D,
    private val old: IndexLookup<T>,
    private val new: IndexLookup<T>,
) : DiffHook<E> {
    private val ops: MutableList<DiffOp> = mutableListOf()

    companion object {
        /** Creates a new compact hook wrapping another hook. */
        fun <E, T, D : DiffHook<E>> new(d: D, old: IndexLookup<T>, new: IndexLookup<T>): Compact<E, T, D> =
            Compact(d, old, new)
    }

    /** Extracts the inner hook. */
    fun intoInner(): D = d

    /** Returns the wrapped hook. */
    fun asRef(): D = d

    /** Returns the wrapped hook. */
    fun asMut(): D = d

    override fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<E> {
        ops += DiffOp.Equal(
            oldIndex = oldIndex,
            newIndex = newIndex,
            len = len,
        )
        return DiffHookResult.Ok
    }

    override fun delete(oldIndex: Int, oldLen: Int, newIndex: Int): DiffHookResult<E> {
        ops += DiffOp.Delete(
            oldIndex = oldIndex,
            oldLen = oldLen,
            newIndex = newIndex,
        )
        return DiffHookResult.Ok
    }

    override fun insert(oldIndex: Int, newIndex: Int, newLen: Int): DiffHookResult<E> {
        ops += DiffOp.Insert(
            oldIndex = oldIndex,
            newIndex = newIndex,
            newLen = newLen,
        )
        return DiffHookResult.Ok
    }

    override fun finish(): DiffHookResult<E> {
        cleanupDiffOps(old, new, ops)
        for (op in ops) {
            when (val applied = op.applyToHook(d)) {
                is DiffHookResult.Err -> return applied
                is DiffHookResult.Ok -> {}
            }
        }
        return d.finish()
    }
}

/**
 * Walks through all edits and shifts them up and then down, trying to see if
 * they run into similar edits which can be merged.
 */
fun <T> cleanupDiffOps(old: IndexLookup<T>, new: IndexLookup<T>, ops: MutableList<DiffOp>) {
    var pointer = 0
    while (pointer < ops.size) {
        if (ops[pointer].tag() == DiffTag.Delete) {
            pointer = shiftDiffOpsUp(ops, old, new, pointer)
            pointer = shiftDiffOpsDown(ops, old, new, pointer)
        }
        pointer += 1
    }

    pointer = 0
    while (pointer < ops.size) {
        if (ops[pointer].tag() == DiffTag.Insert) {
            pointer = shiftDiffOpsUp(ops, old, new, pointer)
            pointer = shiftDiffOpsDown(ops, old, new, pointer)
        }
        pointer += 1
    }
}

private fun <T> shiftDiffOpsUp(
    ops: MutableList<DiffOp>,
    old: IndexLookup<T>,
    new: IndexLookup<T>,
    startPointer: Int,
): Int {
    var pointer = startPointer
    while (pointer > 0) {
        val prevOp = ops[pointer - 1].copyOp()
        val thisOp = ops[pointer].copyOp()
        when (thisOp.tag() to prevOp.tag()) {
            DiffTag.Insert to DiffTag.Equal -> {
                val suffixLen = commonSuffixLen(old, prevOp.oldRange(), new, thisOp.newRange())
                if (suffixLen > 0) {
                    if (ops.getOrNull(pointer + 1)?.tag() == DiffTag.Equal) {
                        ops[pointer + 1].growLeft(suffixLen)
                    } else {
                        ops.add(
                            pointer + 1,
                            DiffOp.Equal(
                                oldIndex = prevOp.oldRange().exclusiveEnd() - suffixLen,
                                newIndex = thisOp.newRange().exclusiveEnd() - suffixLen,
                                len = suffixLen,
                            ),
                        )
                    }
                    ops[pointer].shiftLeft(suffixLen)
                    ops[pointer - 1].shrinkLeft(suffixLen)

                    if (ops[pointer - 1].isEmpty()) {
                        ops.removeAt(pointer - 1)
                        pointer -= 1
                    }
                } else if (ops[pointer - 1].isEmpty()) {
                    ops.removeAt(pointer - 1)
                    pointer -= 1
                } else {
                    break
                }
            }

            DiffTag.Delete to DiffTag.Equal -> {
                val suffixLen = commonSuffixLen(old, prevOp.oldRange(), new, thisOp.newRange())
                if (suffixLen != 0) {
                    if (ops.getOrNull(pointer + 1)?.tag() == DiffTag.Equal) {
                        ops[pointer + 1].growLeft(suffixLen)
                    } else {
                        val oldRange = prevOp.oldRange()
                        ops.add(
                            pointer + 1,
                            DiffOp.Equal(
                                oldIndex = oldRange.exclusiveEnd() - suffixLen,
                                newIndex = thisOp.newRange().exclusiveEnd() - suffixLen,
                                len = oldRange.rangeLen() - suffixLen,
                            ),
                        )
                    }
                    ops[pointer].shiftLeft(suffixLen)
                    ops[pointer - 1].shrinkLeft(suffixLen)

                    if (ops[pointer - 1].isEmpty()) {
                        ops.removeAt(pointer - 1)
                        pointer -= 1
                    }
                } else if (ops[pointer - 1].isEmpty()) {
                    ops.removeAt(pointer - 1)
                    pointer -= 1
                } else {
                    break
                }
            }

            DiffTag.Insert to DiffTag.Delete,
            DiffTag.Delete to DiffTag.Insert,
            -> {
                ops.swap(pointer - 1, pointer)
                pointer -= 1
            }

            DiffTag.Insert to DiffTag.Insert -> {
                ops[pointer - 1].growRight(thisOp.newRange().rangeLen())
                ops.removeAt(pointer)
                pointer -= 1
            }

            DiffTag.Delete to DiffTag.Delete -> {
                ops[pointer - 1].growRight(thisOp.oldRange().rangeLen())
                ops.removeAt(pointer)
                pointer -= 1
            }

            else -> error("unexpected tag")
        }
    }
    return pointer
}

private fun <T> shiftDiffOpsDown(
    ops: MutableList<DiffOp>,
    old: IndexLookup<T>,
    new: IndexLookup<T>,
    startPointer: Int,
): Int {
    var pointer = startPointer
    while (pointer + 1 < ops.size) {
        val nextOp = ops[pointer + 1].copyOp()
        val thisOp = ops[pointer].copyOp()
        when (thisOp.tag() to nextOp.tag()) {
            DiffTag.Insert to DiffTag.Equal -> {
                val prefixLen = commonPrefixLen(old, nextOp.oldRange(), new, thisOp.newRange())
                if (prefixLen > 0) {
                    if (ops.getOrNull(pointer - 1)?.tag() == DiffTag.Equal) {
                        ops[pointer - 1].growRight(prefixLen)
                    } else {
                        ops.add(
                            pointer,
                            DiffOp.Equal(
                                oldIndex = nextOp.oldRange().first,
                                newIndex = thisOp.newRange().first,
                                len = prefixLen,
                            ),
                        )
                        pointer += 1
                    }
                    ops[pointer].shiftRight(prefixLen)
                    ops[pointer + 1].shrinkRight(prefixLen)

                    if (ops[pointer + 1].isEmpty()) {
                        ops.removeAt(pointer + 1)
                    }
                } else if (ops[pointer + 1].isEmpty()) {
                    ops.removeAt(pointer + 1)
                } else {
                    break
                }
            }

            DiffTag.Delete to DiffTag.Equal -> {
                val prefixLen = commonPrefixLen(old, nextOp.oldRange(), new, thisOp.newRange())
                if (prefixLen > 0) {
                    if (ops.getOrNull(pointer - 1)?.tag() == DiffTag.Equal) {
                        ops[pointer - 1].growRight(prefixLen)
                    } else {
                        ops.add(
                            pointer,
                            DiffOp.Equal(
                                oldIndex = nextOp.oldRange().first,
                                newIndex = thisOp.newRange().first,
                                len = prefixLen,
                            ),
                        )
                        pointer += 1
                    }
                    ops[pointer].shiftRight(prefixLen)
                    ops[pointer + 1].shrinkRight(prefixLen)

                    if (ops[pointer + 1].isEmpty()) {
                        ops.removeAt(pointer + 1)
                    }
                } else if (ops[pointer + 1].isEmpty()) {
                    ops.removeAt(pointer + 1)
                } else {
                    break
                }
            }

            DiffTag.Insert to DiffTag.Delete,
            DiffTag.Delete to DiffTag.Insert,
            -> {
                ops.swap(pointer, pointer + 1)
                pointer += 1
            }

            DiffTag.Insert to DiffTag.Insert -> {
                ops[pointer].growRight(nextOp.newRange().rangeLen())
                ops.removeAt(pointer + 1)
            }

            DiffTag.Delete to DiffTag.Delete -> {
                ops[pointer].growRight(nextOp.oldRange().rangeLen())
                ops.removeAt(pointer + 1)
            }

            else -> error("unexpected tag")
        }
    }
    return pointer
}

private fun MutableList<DiffOp>.swap(a: Int, b: Int) {
    val tmp = this[a]
    this[a] = this[b]
    this[b] = tmp
}

private fun DiffOp.copyOp(): DiffOp =
    when (this) {
        is DiffOp.Equal -> copy()
        is DiffOp.Delete -> copy()
        is DiffOp.Insert -> copy()
        is DiffOp.Replace -> copy()
    }
