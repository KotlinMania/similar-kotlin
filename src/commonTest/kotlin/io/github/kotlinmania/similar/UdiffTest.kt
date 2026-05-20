// port-lint: source udiff.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.text.TextDiff
import kotlin.test.Test
import kotlin.test.assertEquals

class UdiffTest {
    @Test
    fun testUnifiedDiff() {
        val diff = TextDiff.fromLines(
            "a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\ns\nt\nu\nv\nw\nx\ny\nz\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\nR\nS\nT\nU\nV\nW\nX\nY\nZ",
            "a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\nq\nr\nS\nt\nu\nv\nw\nx\ny\nz\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\no\nP\nQ\nR\nS\nT\nU\nV\nW\nX\nY\nZ",
        )

        assertEquals(
            "--- a.txt\n" +
                "+++ b.txt\n" +
                "@@ -16,7 +16,7 @@\n" +
                " p\n" +
                " q\n" +
                " r\n" +
                "-s\n" +
                "+S\n" +
                " t\n" +
                " u\n" +
                " v\n" +
                "@@ -38,7 +38,7 @@\n" +
                " L\n" +
                " M\n" +
                " N\n" +
                "-O\n" +
                "+o\n" +
                " P\n" +
                " Q\n" +
                " R\n",
            diff.unifiedDiff().header("a.txt", "b.txt").toString(),
        )
    }

    @Test
    fun testEmptyUnifiedDiff() {
        val diff = TextDiff.fromLines("abc", "abc")
        assertEquals("", diff.unifiedDiff().header("a.txt", "b.txt").toString())
    }

    @Test
    fun testUnifiedDiffNewlineHint() {
        val diff = TextDiff.fromLines("a\n", "b")
        assertEquals(
            "--- a.txt\n+++ b.txt\n@@ -1 +1 @@\n-a\n+b\n\\ No newline at end of file\n",
            diff.unifiedDiff().header("a.txt", "b.txt").toString(),
        )
        assertEquals(
            "--- a.txt\n+++ b.txt\n@@ -1 +1 @@\n-a\n+b\n",
            diff.unifiedDiff().missingNewlineHint(false).header("a.txt", "b.txt").toString(),
        )
    }
}
