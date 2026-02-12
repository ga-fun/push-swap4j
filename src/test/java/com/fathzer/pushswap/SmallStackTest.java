package com.fathzer.pushswap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SmallStackTest extends IStackTest {

    @Override
    protected IStack createStack() {
        return new FastStack();
    }

    @Override
    protected IStack createStack(int[] elements) {
        FastStack stack = new FastStack();
        for (int i=elements.length-1; i>=0; i--) {
            stack.push(elements[i]);
        }
        return stack;
    }

    @Test
    void testCopyConstructor() {
        IStack stack = new Stack(new int[]{1, 2, 3});
        IStack copy = new FastStack(stack);
        assertEquals(stack.size(), copy.size());
        for (int i = 0; i < stack.size(); i++) {
            assertEquals(stack.get(i), copy.get(i));
        }
    }

    @Test
    void testEqualsAndHashCode() {
        FastStack original = new FastStack(new Stack(new int[]{1, 2, 3}));
        FastStack stack = new FastStack(original);
        long expectedData = stack.getData();
        int expectedHashCode = stack.hashCode();

        // Test with rotations
        stack.rotateForward();
        stack.rotateBackward();
        assertEquals(expectedData, stack.getData());
        assertEquals(expectedHashCode, stack.hashCode());
        assertEquals(original, stack);
        stack.rotateBackward();
        stack.rotateForward();
        assertEquals(expectedData, stack.getData());
        assertEquals(expectedHashCode, stack.hashCode());
        assertEquals(original, stack);

        // Test with swaps
        stack.swap();
        stack.swap();
        assertEquals(expectedData, stack.getData());
        assertEquals(expectedHashCode, stack.hashCode());
        assertEquals(original, stack);

        // Test with pop / push
        int head = stack.pop();
        stack.push(head);
        assertEquals(expectedData, stack.getData());
        assertEquals(expectedHashCode, stack.hashCode());
        assertEquals(original, stack);
        
        // Test with push / pop
        stack.push(4);
        stack.pop();
        assertEquals(expectedData, stack.getData());
        assertEquals(expectedHashCode, stack.hashCode());
        assertEquals(original, stack);
    }
}
