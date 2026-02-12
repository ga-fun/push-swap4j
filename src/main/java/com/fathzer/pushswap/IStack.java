package com.fathzer.pushswap;

import java.util.Iterator;
import java.util.stream.IntStream;

public interface IStack extends Iterable<Integer> {
    default boolean isEmpty() {
        return size() == 0;
    }

    int size();
    
    int get(int index);

    default int pop() {
        return get(0);
    }

    void push(int value);

    default void swap() {
        int first = pop();
        int second = pop();
        push(first);
        push(second);
    }
        
    void rotateForward();
    
    void rotateBackward();

    @Override
    default Iterator<Integer> iterator() {
        return IntStream.range(0, size()).mapToObj(this::get).iterator();
    }

    default boolean isSorted() {
        for (int i = 0; i < size() - 1; i++) {
            if (get(i) > get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    default String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    default int[] toArray() {
        return IntStream.range(0, size()).map(this::get).toArray();
    }
}
