// port-lint: source iter.rs
package io.github.kotlinmania.similar

private fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }

/** Iterator for [DiffOp.iterChanges]. */
class ChangesIter<T>(
    private val old: IndexLookup<T>,
    private val new: IndexLookup<T>,
    op: DiffOp,
) : Iterator<Change<T>> {
    private val oldRange: IntRange
    private val newRange: IntRange
    private var oldIndex: Int
    private var newIndex: Int
    private var oldI: Int
    private var newI: Int
    private val tag: DiffTag

    init {
        val tuple = op.asTagTuple()
        tag = tuple.tag
        oldRange = tuple.oldRange
        newRange = tuple.newRange
        oldIndex = oldRange.first
        newIndex = newRange.first
        oldI = oldRange.first
        newI = newRange.first
    }

    override fun hasNext(): Boolean =
        when (tag) {
            DiffTag.Equal -> oldI < oldRange.exclusiveEnd()
            DiffTag.Delete -> oldI < oldRange.exclusiveEnd()
            DiffTag.Insert -> newI < newRange.exclusiveEnd()
            DiffTag.Replace -> oldI < oldRange.exclusiveEnd() || newI < newRange.exclusiveEnd()
        }

    override fun next(): Change<T> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        return when (tag) {
            DiffTag.Equal -> {
                val value = old[oldI]
                oldI += 1
                oldIndex += 1
                newIndex += 1
                Change(
                    ChangeTag.Equal,
                    oldIndex - 1,
                    newIndex - 1,
                    value,
                )
            }

            DiffTag.Delete -> {
                val value = old[oldI]
                oldI += 1
                oldIndex += 1
                Change(
                    ChangeTag.Delete,
                    oldIndex - 1,
                    null,
                    value,
                )
            }

            DiffTag.Insert -> {
                val value = new[newI]
                newI += 1
                newIndex += 1
                Change(
                    ChangeTag.Insert,
                    null,
                    newIndex - 1,
                    value,
                )
            }

            DiffTag.Replace -> {
                if (oldI < oldRange.exclusiveEnd()) {
                    val value = old[oldI]
                    oldI += 1
                    oldIndex += 1
                    Change(
                        ChangeTag.Delete,
                        oldIndex - 1,
                        null,
                        value,
                    )
                } else {
                    val value = new[newI]
                    newI += 1
                    newIndex += 1
                    Change(
                        ChangeTag.Insert,
                        null,
                        newIndex - 1,
                        value,
                    )
                }
            }
        }
    }
}

/** Iterator for `TextDiff.iterAllChanges`. */
class AllChangesIter<T>(
    private val old: List<T>,
    private val new: List<T>,
    private var ops: List<DiffOp>,
) : Iterator<Change<T>> {
    private var currentIter: ChangesIter<T>? = null
    private val oldLookup = old.asLookup()
    private val newLookup = new.asLookup()

    override fun hasNext(): Boolean {
        while (true) {
            val iter = currentIter
            if (iter != null && iter.hasNext()) {
                return true
            }
            currentIter = null
            if (ops.isEmpty()) {
                return false
            }
            currentIter = ChangesIter(oldLookup, newLookup, ops.first())
            ops = ops.drop(1)
        }
    }

    override fun next(): Change<T> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        return currentIter!!.next()
    }
}
