package com.fathzer.pushswap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.StreamSupport;

abstract class IStackTest {

    protected abstract IStack createStack();
    protected abstract IStack createStack(int[] elements);
    
    @Test
    void testDefaultConstructor() {
        IStack stack = createStack();
        assertTrue(stack.isEmpty(), "New stack should be empty");
        assertEquals(0, stack.size(), "New stack should have size 0");
    }
    
    @Test
    void testConstructorWithArray() {
        int[] elements = {3, 2, 1};
        IStack stack = createStack(elements);
        
        assertFalse(stack.isEmpty(), "Stack should not be empty");
        assertEquals(3, stack.size(), "Stack should have size 3");
        assertEquals(3, stack.get(0), "First element should be 3");
        assertEquals(2, stack.get(1), "Second element should be 2");
        assertEquals(1, stack.get(2), "Third element should be 1");
    }
    
    @Test
    void testConstructorWithEmptyArray() {
        int[] elements = {};
        IStack stack = createStack(elements);
        
        assertTrue(stack.isEmpty(), "Stack should be empty");
        assertEquals(0, stack.size(), "Stack should have size 0");
    }
    
    @Test
    void testPushAndGet() {
        IStack stack = createStack();
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
        IStack stack = createStack();
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
        IStack rotatedStack = createStack(new int[]{8, 3, 5});
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
        IStack stack = createStack();
        assertTrue(stack.isEmpty(), "New stack should be empty");
        
        stack.push(5);
        assertFalse(stack.isEmpty(), "Stack with elements should not be empty");
        
        stack.pop();
        assertTrue(stack.isEmpty(), "Stack should be empty after popping all elements");
    }
    
    @Test
    void testSize() {
        IStack stack = createStack();
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
        IStack stack = createStack();
        stack.push(5);
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(-1), "Should throw exception for negative index");
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(1), "Should throw exception for index equal to size");
        
        assertThrows(IndexOutOfBoundsException.class, () -> stack.get(2), "Should throw exception for index greater than size");
    }
    
    @Test
    void testRotateForward() {
        IStack stack = createStack();
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
        IStack stack = createStack();
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
    void testToArray() {
        IStack stack = createStack(new int[]{2, 3, 1});
        assertArrayEquals(new int[]{2, 3, 1}, stack.toArray());
        stack.rotateBackward();
        assertArrayEquals(new int[]{1, 2, 3}, stack.toArray());

        // Check that element removal is working
        stack.pop();
        assertArrayEquals(new int[]{2, 3}, stack.toArray());
    }

    @Test
    void testIterator() {
        IStack stack = createStack(new int[]{2, 3, 1});
        stack.rotateBackward();
        int[] array = StreamSupport.stream(stack.spliterator(), false).mapToInt(i->i).toArray();
        assertArrayEquals(new int[]{1, 2, 3}, array);
    }
}
