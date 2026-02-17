package io.github.jovanzdravkovic;

import io.github.jovanzdravkovic.models.Cell;
import io.github.jovanzdravkovic.models.Style;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class ScrollbackBufferTest {

    private ScrollbackBuffer scrollbackBuffer;

    @BeforeEach
    void setup() {
        scrollbackBuffer = new ScrollbackBuffer(4, 4, 5, (byte)0, (byte)0, null);
    }

    /**
     * Verifies that writing cells into an empty scrollback buffer stores
     * the content correctly and produces the expected string representation.
     */
    @Test
    void testWriteIntoEmptyBuffer() {
        assertEquals("", scrollbackBuffer.toString());
        scrollbackBuffer.writeCells(new Cell[] {
                new Cell('A', (byte)0, (byte)0, null),
                new Cell('B', (byte)0, (byte)0, null),
                new Cell('C', (byte)0, (byte)0, null),
                new Cell('D', (byte)0, (byte)0, null),
                new Cell('E', (byte)0, (byte)0, null)
        });
        assertEquals("ABCDE", scrollbackBuffer.toString());
    }

    /**
     * Verifies that writing into a full scrollback buffer causes the oldest
     * content to be discarded and the new cells to be at the front of the buffer.
     */
    @Test
    void testWriteIntoFullBuffer() {
        assertEquals("", scrollbackBuffer.toString());

        Cell[] arrayToFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize()];
        StringBuilder filledBufferExpectedResult = new StringBuilder();
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize(); i++) {
            arrayToFillBuffer[i] = new Cell('X', (byte)0, (byte)0, null);
            filledBufferExpectedResult.append('X');
        }

        scrollbackBuffer.writeCells(arrayToFillBuffer);
        assertEquals(filledBufferExpectedResult.toString(), scrollbackBuffer.toString());

        Cell[] arrayToOverflowBuffer = new Cell[] {
                new Cell('A', (byte)0, (byte)0, null),
                new Cell('B', (byte)0, (byte)0, null),
                new Cell('C', (byte)0, (byte)0, null),
                new Cell('D', (byte)0, (byte)0, null),
                new Cell('E', (byte)0, (byte)0, null)
        };
        filledBufferExpectedResult.replace(
                scrollbackBuffer.getMaximumScrollbackSize() - arrayToOverflowBuffer.length,
                scrollbackBuffer.getMaximumScrollbackSize(),
                "ABCDE"
                );
        scrollbackBuffer.writeCells(arrayToOverflowBuffer);
        assertEquals(filledBufferExpectedResult.toString(), scrollbackBuffer.toString());
    }

    /**
     * Verifies that null cells explicitly written into the buffer are treated differently (treated as cells with ' ' character) than null cells, and
     * reflected correctly in the output.
     */
    @Test
    void testWriteSomeEmptyCells() {
        assertEquals("", scrollbackBuffer.toString());
        Cell[] arrayWithNulls = new Cell[] {
                new Cell('A', (byte)0, (byte)0, null),
                new Cell('B', (byte)0, (byte)0, null),
                new Cell('C', (byte)0, (byte)0, null),
                null,
                null,
                null,
                new Cell('D', (byte)0, (byte)0, null),
                new Cell('E', (byte)0, (byte)0, null),
                new Cell('F', (byte)0, (byte)0, null)
        };
        scrollbackBuffer.writeCells(arrayWithNulls);
        assertEquals("ABC   DEF", scrollbackBuffer.toString());
    }

    /**
     * Verifies that characters are correctly returned for valid
     * row and column positions within the scrollback buffer.
     */
    @Test
    void testCharAtValidPosition() {
        assertEquals("", scrollbackBuffer.toString());
        Cell[] arrayToFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize()];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize(); i++) {
            arrayToFillBuffer[i] = new Cell((char)(i + 'A'), (byte)0, (byte)0, null);
        }
        scrollbackBuffer.writeCells(arrayToFillBuffer);
        assertEquals('G', scrollbackBuffer.charAtPosition(1, 1));
        assertEquals('B', scrollbackBuffer.charAtPosition(0, 1));
        assertEquals('M', scrollbackBuffer.charAtPosition(2, 2));
    }

    /**
     * Verifies that empty/uninitialized positions return a space
     * character when queried.
     */
    @Test
    void testCharAtEmptyPosition() {
        assertEquals("", scrollbackBuffer.toString());
        assertEquals("", scrollbackBuffer.toString());
        Cell[] arrayToPartiallyFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize() - 5];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize() - 5; i++) {
            arrayToPartiallyFillBuffer[i] = new Cell((char)(i + 'A'), (byte)0, (byte)0, null);
        }
        scrollbackBuffer.writeCells(arrayToPartiallyFillBuffer);
        assertEquals('B', scrollbackBuffer.charAtPosition(0, 1));
        assertEquals('O', scrollbackBuffer.charAtPosition(2, 4));
        assertEquals(' ', scrollbackBuffer.charAtPosition(3, 1));
        assertEquals(' ', scrollbackBuffer.charAtPosition(3, 4));
    }

    /**
     * Verifies that style information is correctly returned for
     * cells at valid positions in the scrollback buffer.
     */
    @Test
    void testStylesAtValidPosition() {
        assertEquals("", scrollbackBuffer.toString());
        Cell[] arrayToFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize()];
        EnumSet<Style> style1 = EnumSet.of(Style.BOLD);
        EnumSet<Style> style2 = EnumSet.of(Style.ITALIC, Style.UNDERLINE);
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize(); i++) {
            arrayToFillBuffer[i] = new Cell('X', (byte)0, (byte)0, i % 2 == 0 ? style1 : style2);
        }
        scrollbackBuffer.writeCells(arrayToFillBuffer);
        assertEquals(style1, scrollbackBuffer.stylesAtPosition(0, 0));
        assertEquals(style2, scrollbackBuffer.stylesAtPosition(0, 1));
        assertEquals(style1, scrollbackBuffer.stylesAtPosition(2, 2));
    }

    /**
     * Verifies that querying styles at empty positions returns null.
     */
    @Test
    void testStylesAtEmptyPosition() {
        assertEquals("", scrollbackBuffer.toString());

        EnumSet<Style> style1 = EnumSet.of(Style.BOLD);
        EnumSet<Style> style2 = EnumSet.of(Style.ITALIC, Style.UNDERLINE);

        Cell[] arrayToPartiallyFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize() - 5];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize() - 5; i++) {
            arrayToPartiallyFillBuffer[i] = new Cell('X', (byte)0, (byte)0, i % 2 == 0 ? style1 : style2);
        }
        scrollbackBuffer.writeCells(arrayToPartiallyFillBuffer);

        assertEquals(style2, scrollbackBuffer.stylesAtPosition(0, 1));
        assertEquals(style1, scrollbackBuffer.stylesAtPosition(2, 4));
        assertNull(scrollbackBuffer.stylesAtPosition(3, 1));
        assertNull(scrollbackBuffer.stylesAtPosition(3, 4));
    }

    /**
     * Verifies that full existing lines are returned correctly
     * from the scrollback buffer.
     */
    @Test
    void getExistingLine() {
        assertEquals("", scrollbackBuffer.toString());

        Cell[] arrayToFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize()];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize(); i++) {
            arrayToFillBuffer[i] = new Cell((char)(i + 'A'), (byte)0, (byte)0, null);
        }
        scrollbackBuffer.writeCells(arrayToFillBuffer);

        assertEquals("ABCDE", scrollbackBuffer.getLine(0));
        assertEquals("FGHIJ", scrollbackBuffer.getLine(1));
        assertEquals("KLMNO", scrollbackBuffer.getLine(2));
        assertEquals("PQRST", scrollbackBuffer.getLine(3));
    }

    /**
     * Verifies that requesting a line beyond the available content
     * returns null.
     */
    @Test
    void getNonExistingLine() {
        assertEquals("", scrollbackBuffer.toString());

        Cell[] arrayToFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize() - 5];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize() - 5; i++) {
            arrayToFillBuffer[i] = new Cell((char)(i + 'A'), (byte)0, (byte)0, null);
        }
        scrollbackBuffer.writeCells(arrayToFillBuffer);

        assertEquals("ABCDE", scrollbackBuffer.getLine(0));
        assertEquals("FGHIJ", scrollbackBuffer.getLine(1));
        assertEquals("KLMNO", scrollbackBuffer.getLine(2));
        assertNull(scrollbackBuffer.getLine(3));
    }

    /**
     * Verifies that a partially filled final line is returned
     * with only the existing characters. An edge case specifically mentioned in the getLine method.
     */
    @Test
    void getExistingPartialLine() {
        assertEquals("", scrollbackBuffer.toString());

        Cell[] arrayToPartiallyFillBuffer = new Cell[scrollbackBuffer.getMaximumScrollbackSize() - 3];
        for(int i = 0; i < scrollbackBuffer.getMaximumScrollbackSize() - 3; i++) {
            arrayToPartiallyFillBuffer[i] = new Cell((char)(i + 'A'), (byte)0, (byte)0, null);
        }
        scrollbackBuffer.writeCells(arrayToPartiallyFillBuffer);

        assertEquals("ABCDE", scrollbackBuffer.getLine(0));
        assertEquals("FGHIJ", scrollbackBuffer.getLine(1));
        assertEquals("KLMNO", scrollbackBuffer.getLine(2));
        assertEquals("PQ", scrollbackBuffer.getLine(3));
    }
}