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
}
