package io.github.jovanzdravkovic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {
    private Cursor cursor;

    @BeforeEach
    void setUp() {
        cursor = new Cursor(3, 4);
    }

    /**
     * Tests that initial position, row and column of a newly created cursor are zero.
     */
    @Test
    void testInitialPosition() {
        assertEquals(0, cursor.getPosition());
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getColumn());
    }

    /**
     * Verifies that setting the cursor to a position within terminal bounds
     * correctly updates the internal row and column state.
     */
    @Test
    void testSetPositionWithinBounds() {
        cursor.setPosition(2, 3);
        assertEquals(2, cursor.getRow());
        assertEquals(3, cursor.getColumn());
    }

    /**
     * Verifies that setting the cursor to a position out of the terminal bounds,
     * clamps the position to the nearest valid values and correctly updates the internal row and column state.
     */
    @Test
    void testSetPositionClamped() {
        cursor.setPosition(-1, 5);
        assertEquals(0, cursor.getRow());
        assertEquals(3, cursor.getColumn());
    }

    /**
     * Tests the cursor wrapping to next row when moving right, and wrapping to the previous row when moving left.
     */
    @Test
    void testWrappingRightAndLeft() {
        cursor.moveRight(5);
        assertEquals(1, cursor.getRow());
        assertEquals(1, cursor.getColumn());

        cursor.moveLeft(2);
        assertEquals(0, cursor.getRow());
        assertEquals(3, cursor.getColumn());
    }

    /**
     * Verifies that the cursor stops at the screen boundaries when moving left or right.
     * Moving right beyond the last cell clamps to the last cell,
     * and moving left beyond the first cell clamps to the first cell.
     */
    @Test
    void testStoppingLeftAndRight() {
        cursor.moveRight(50);
        assertTrue(cursor.isLastCell());
        cursor.moveLeft(50);
        assertEquals(0, cursor.getPosition());
    }

    /**
     * Tests the cursor moving to upwards and downwards rows.
     */
    @Test
    void testMoveUpAndDown() {
        cursor.setPosition(1, 2);
        cursor.moveUp(1);
        assertEquals(0, cursor.getRow());
        assertEquals(2, cursor.getColumn());

        cursor.moveDown(2);
        assertEquals(2, cursor.getRow());
        assertEquals(2, cursor.getColumn());
    }

    /**
     * Tests the cursor moving to upwards and downwards rows, stopping at the first and last rows.
     */
    @Test
    void testStoppingUpAndDown() {
        cursor.setPosition(2, 2);
        cursor.moveUp(4);
        assertEquals(0, cursor.getRow());
        assertEquals(2, cursor.getColumn());

        cursor.moveDown(5);
        assertEquals(2, cursor.getRow());
        assertEquals(2, cursor.getColumn());
    }

    /**
     * Tests if the cursor properly moves to the beginning of the row.
     */
    @Test
    void testMoveToBeginning() {
        cursor.setPosition(2, 3);
        cursor.moveToTheBeginning();
        assertEquals(2, cursor.getRow());
        assertEquals(0, cursor.getColumn());
    }

    /**
     * Verifies that isLastCell() correctly identifies whether the cursor
     * is positioned at the last cell of the terminal screen.
     */
    @Test
    void testIsLastCell() {
        assertFalse(cursor.isLastCell());
        cursor.setPosition(2, 3);
        assertTrue(cursor.isLastCell());
    }

    /**
     * Tests that resizing the terminal to a larger size preserves the cursor's position index
     * and adjusts the row and column values accordingly.
     */
    @Test
    void resizeToLargerTerminal() {
        cursor.setPosition(2, 2);
        cursor.resizeTerminal(4, 5);
        assertEquals(10, cursor.getPosition());
        assertEquals(2, cursor.getRow());
        assertEquals(0, cursor.getColumn());
    }

    /**
     * Tests that resizing the terminal to a smaller size clamps the cursor to the last valid cell,
     * if its previous position exceeds the new terminal bounds.
     */
    @Test
    void resizeToSmallerTerminal() {
        cursor.setPosition(2, 2);
        cursor.resizeTerminal(2, 2);
        assertTrue(cursor.isLastCell());
        assertEquals(1, cursor.getRow());
        assertEquals(1, cursor.getColumn());
    }
}