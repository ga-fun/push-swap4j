package com.fathzer.pushswap;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    private List<Integer> list;  // Index 0 = haut de la pile

    public Stack() {
        this.list = new ArrayList<>();
    }

    public Stack(int[] elements) {
        this.list = new ArrayList<>();
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
        if (list.isEmpty()) return 0;
        
        int targetIndex = -1;
        int closestBigger = Integer.MAX_VALUE;
        
        for (int i = 0; i < list.size(); i++) {
            int elem = list.get(i);
            if (elem > value && elem < closestBigger) {
                closestBigger = elem;
                targetIndex = i;
            }
        }
        
        // Si pas trouvé (value est plus grand que tout dans A),
        // on doit le placer avant le plus petit élément de A
        if (targetIndex == -1) {
            int minIndex = 0;
            int minValue = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) < minValue) {
                    minValue = list.get(i);
                    minIndex = i;
                }
            }
            targetIndex = minIndex;
        }
        
        return targetIndex;
    }
}
