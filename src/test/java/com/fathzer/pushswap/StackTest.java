package com.fathzer.pushswap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.StreamSupport;

class StackTest {
    
    @Test
    void testDefaultConstructor() {
        Stack stack = new Stack();
        assertTrue(stack.isEmpty(), "New stack should be empty");
        assertEquals(0, stack.size(), "New stack should have size 0");
    }
    
    @Test
    void testConstructorWithArray() {
        int[] elements = {3, 2, 1};
        Stack stack = new Stack(elements);
        
        assertFalse(stack.isEmpty(), "Stack should not be empty");
        assertEquals(3, stack.size(), "Stack should have size 3");
        assertEquals(3, stack.get(0), "First element should be 3");
        assertEquals(2, stack.get(1), "Second element should be 2");
        assertEquals(1, stack.get(2), "Third element should be 1");
    }
    
    @Test
    void testConstructorWithEmptyArray() {
        int[] elements = {};
        Stack stack = new Stack(elements);
        
        assertTrue(stack.isEmpty(), "Stack should be empty");
        assertEquals(0, stack.size(), "Stack should have size 0");
    }
    
    @Test
    void testPushAndGet() {
        Stack stack = new Stack();
        stack.push(5);
        stack.push(3);
        stack.push(8);
        
        assertEquals(3, stack.size(), "Stack should have size 3");
        assertEquals(8, stack.get(0), "Top element should be 8");
        assertEquals(3, stack.get(1), "Second element should be 3");
        assertEquals(5, stack.get(2), "Third element should be 5");
    }
    
    @Test
    void testPop() {
        Stack stack = new Stack();
        assertThrows(RuntimeException.class, stack::pop, "Should throw exception when popping from empty stack");

        stack.push(5);
        stack.push(3);
        stack.push(8);
        
        assertEquals(8, stack.pop(), "Should pop 8");
        assertEquals(2, stack.size(), "Stack should have size 2");
        assertEquals(3, stack.get(0), "New top should be 3");
        assertEquals(5, stack.get(1), "Second element should be 5");
        
        assertEquals(3, stack.pop(), "Should pop 3");
        assertEquals(1, stack.size(), "Stack should have size 1");
        assertEquals(5, stack.get(0), "New top should be 5");
        
        assertEquals(5, stack.pop(), "Should pop 5");
        assertTrue(stack.isEmpty(), "Stack should be empty");
        assertThrows(RuntimeException.class, stack::pop, "Should throw exception when popping from empty stack");

        // Try on rotated stack
        Stack rotatedStack = new Stack(new int[]{8, 3, 5});
        rotatedStack.rotateForward();
        assertEquals(3, rotatedStack.pop(), "Should pop 3");
        assertEquals(5, rotatedStack.get(0), "New top should be 5");
        assertEquals(8, rotatedStack.get(1), "Second element should be 8");
        assertEquals(5, rotatedStack.pop(), "Should pop 5");
        assertEquals(8, rotatedStack.get(0), "New top should be 8");
        assertEquals(8, rotatedStack.pop(), "Should pop 8");
        assertTrue(rotatedStack.isEmpty(), "Stack should be empty");
        assertThrows(RuntimeException.class, stack::pop, "Should throw exception when popping from empty stack");
    }
    
    @Test
    void testIsEmpty() {
        Stack stack = new Stack();
        assertTrue(stack.isEmpty(), "New stack should be empty");
        
        stack.push(5);
        assertFalse(stack.isEmpty(), "Stack with elements should not be empty");
        
        stack.pop();
        assertTrue(stack.isEmpty(), "Stack should be empty after popping all elements");
    }
    
    @Test
    void testSize() {
        Stack stack = new Stack();
        assertEquals(0, stack.size(), "Empty stack should have size 0");
        
        stack.push(5);
        assertEquals(1, stack.size(), "Stack with one element should have size 1");
        
        stack.push(3);
        assertEquals(2, stack.size(), "Stack with two elements should have size 2");
        
        stack.pop();
        assertEquals(1, stack.size(), "Stack should have size 1 after pop");
    }
    
    @Test
    void testGetInvalidIndex() {
        Stack stack = new Stack();
        stack.push(5);
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(-1), "Should throw exception for negative index");
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(1), "Should throw exception for index equal to size");
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(2), "Should throw exception for index greater than size");
    }
    
    @Test
    void testRotateForward() {
        Stack stack = new Stack();
        assertDoesNotThrow((Executable) stack::rotateForward);

        stack.push(3);
        assertDoesNotThrow((Executable) stack::rotateForward);
        assertEquals(3, stack.get(0), "Element should remain unchanged");

        stack.push(2);
        stack.push(1);
        stack.push(0);
        
        // Check [0, 1, 2, 3] -> [1, 2, 3, 0]
        stack.rotateForward();
        assertEquals(4, stack.size(), "Size should remain unchanged");
        assertEquals(1, stack.get(0), "First element should be 1");
        assertEquals(2, stack.get(1), "Second element should be 2");
        assertEquals(3, stack.get(2), "Third element should be 3");
        assertEquals(0, stack.get(3), "Fourth element should be 0");
    }
    
    @Test
    void testRotateBackward() {
        Stack stack = new Stack();
        assertDoesNotThrow((Executable) stack::rotateBackward);

        stack.push(3);
        assertDoesNotThrow((Executable) stack::rotateBackward);
        assertEquals(3, stack.get(0), "Element should remain unchanged");

        stack.push(2);
        stack.push(1);
        stack.push(0);
        
        // Check [0, 1, 2, 3] -> [3, 0, 1, 2]
        stack.rotateBackward();
        assertEquals(4, stack.size(), "Size should remain unchanged");
        assertEquals(3, stack.get(0), "First element should be 3");
        assertEquals(0, stack.get(1), "Second element should be 0");
        assertEquals(1, stack.get(2), "Third element should be 1");
        assertEquals(2, stack.get(3), "Fourth element should be 2");
    }

    @Test
    void testFindTargetPositionSortedStack() {
        // Test with sorted stack: [1, 3, 5]
        Stack sortedStack = new Stack(new int[]{1, 3, 5});
        
        assertEquals(0, sortedStack.findTargetPosition(0), "0 should go before 1");
        assertEquals(1, sortedStack.findTargetPosition(2), "2 should go before 3");
        assertEquals(2, sortedStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(0, sortedStack.findTargetPosition(6), "6 should go before 1 (largest)");
    }
    
    @Test
    void testFindTargetPositionRotatedStack() {
        // Test with rotated stack: [3, 5, 1]
        Stack rotatedStack = new Stack(new int[]{3, 5, 1});
        
        assertEquals(2, rotatedStack.findTargetPosition(0), "0 should go before 1");
        assertEquals(0, rotatedStack.findTargetPosition(2), "3 should go before 4");
        assertEquals(1, rotatedStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(2, rotatedStack.findTargetPosition(6), "6 should go before 1 (largest)");
    }
    
    @Test
    void testFindTargetPositionSingleElement() {
        Stack singleStack = new Stack(new int[]{5});
        
        assertEquals(0, singleStack.findTargetPosition(3), "3 should go before 5");
        assertEquals(0, singleStack.findTargetPosition(4), "4 should go before 5");
        assertEquals(0, singleStack.findTargetPosition(7), "7 should go before 5");
    }
    
    @Test
    void testFindTargetPositionEmpty() {
        Stack emptyStack = new Stack();
        assertEquals(0, emptyStack.findTargetPosition(5));
    }

    @Test
    void testToArray() {
        Stack stack = new Stack(new int[]{2, 3, 1});
        assertArrayEquals(new int[]{2, 3, 1}, stack.toArray());
        stack.rotateBackward();
        assertArrayEquals(new int[]{1, 2, 3}, stack.toArray());

        // Check that element removal is working
        stack.pop();
        assertArrayEquals(new int[]{2, 3}, stack.toArray());
    }

    @Test
    void testIterator() {
        Stack stack = new Stack(new int[]{2, 3, 1});
        stack.rotateBackward();
        int[] array = StreamSupport.stream(stack.spliterator(), false).mapToInt(i->i).toArray();
        assertArrayEquals(new int[]{1, 2, 3}, array);
    }
}
