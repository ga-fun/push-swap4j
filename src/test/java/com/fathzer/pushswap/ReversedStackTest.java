package com.fathzer.pushswap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReversedStackTest extends IStackTest{
    
    @Override
    protected IStack createStack() {
        return new ReversedStack();
    }

    @Override
    protected IStack createStack(int[] elements) {
        return new ReversedStack(elements);
    }
    
    @Test
    void testFindTargetPositionSortedStack() {
        // Test with sorted stack: [1, 3, 5]
    	ReversedStack sortedStack = new ReversedStack(new int[]{1, 3, 5});
        
        assertEquals(0, sortedStack.findTargetPosition(0), "0 should go before 1");
        assertEquals(1, sortedStack.findTargetPosition(2), "2 should go before 3");
        assertEquals(2, sortedStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(0, sortedStack.findTargetPosition(6), "6 should go before 1 (largest)");
    }
    
    @Test
    void testFindTargetPositionRotatedStack() {
        // Test with rotated stack: [3, 5, 1]
    	ReversedStack rotatedStack = new ReversedStack(new int[]{3, 5, 1});
        
        assertEquals(2, rotatedStack.findTargetPosition(0), "0 should go before 1");
        assertEquals(0, rotatedStack.findTargetPosition(2), "3 should go before 4");
        assertEquals(1, rotatedStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(2, rotatedStack.findTargetPosition(6), "6 should go before 1 (largest)");
    }
    
    @Test
    void testFindTargetPositionSingleElement() {
    	ReversedStack singleStack = new ReversedStack(new int[]{5});
        
        assertEquals(0, singleStack.findTargetPosition(3), "3 should go before 5");
        assertEquals(0, singleStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(0, singleStack.findTargetPosition(7), "7 should go before 5");
    }
    
    @Test
    void testFindTargetPositionEmpty() {
    	ReversedStack emptyStack = new ReversedStack();
        assertEquals(0, emptyStack.findTargetPosition(5));
    }
}
