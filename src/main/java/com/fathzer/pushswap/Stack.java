package com.fathzer.pushswap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Stack implements Iterable<Integer> {
    private List<Integer> list;  // Index 0 = haut de la pile

    public Stack() {
        this.list = new ArrayList<>();
    }

    public Stack(int[] elements) {
        this.list = new ArrayList<>(elements.length);
        for (int num : elements) {
            this.list.add(num);
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public int size() {
        return list.size();
    }
    
    public int get(int index) {
        return list.get(index);
    }

    public int pop() {
        return list.removeFirst();
    }

    public void push(int element) {
        list.addFirst(element);
    }

    public void rotateForward() {
        if (list.size() < 2) return;
        int first = list.removeFirst();
        list.addLast(first);
    }
    
    public void rotateBackward() {
        if (list.size() < 2) return;
        int last = list.removeLast();
        list.addFirst(last);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public boolean isSorted() {
        for (int i = 0; i < size() - 1; i++) {
            if (get(i) > get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the target position to insert a value into the stack to keep it sorted in ascending order.
     * <br>The method works also if the stack is rotated.
     * <br>Calling this method on a stack that is not sorted or that contains duplicates returns an undefined result.
     * @param value the value to insert
     * @return the target position. Warning, if the value is greater than all values in the stack, the method returns 0, because the stack remains sorted but rotated.
     */
    public int findTargetPosition(int value) {
        //TODO could probably be optimized with binary search, but be aware of the rotated list case
        if (list.size() < 2) return 0;
        
        int previous = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            int elem = list.get(i);
            if ((previous < value && elem > value) || (previous > elem && (value > previous || value < elem))) {
                // Found consecutive elements that are smaller and greater than the value => Insert here
                // or min and max elements and the value is smaller than min or greater than max => Insert here
                return i;
            }
            previous = elem;
        }
        // smallest value was not detected => smaller or greater than all values in a non rotated stack => insert at first position
        return 0;
    }

    @Override
    public Iterator<Integer> iterator() {
        return list.iterator();
    }
}
