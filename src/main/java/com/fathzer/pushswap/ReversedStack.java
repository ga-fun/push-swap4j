package com.fathzer.pushswap;

import java.util.Arrays;
import java.util.Iterator;

/** A stack with elements stored internally in reversed order (last element is at index 0)
 */
public class ReversedStack implements IStack {
    private int[] list;
    private int size;

    public ReversedStack() {
        this(0);
    }

    public ReversedStack(int intialCapacity) {
        this.list = new int[intialCapacity];
        this.size = 0;
    }

    public ReversedStack(int[] elements) {
        this.size = elements.length;
        this.list = new int[size];
        for (int i = 0; i < size; i++) {
            this.list[i] = elements[size - 1 - i];
        }
    }

    @Override
    public int size() {
        return size;
    }
    
    public int get(int index) {
        if (index>=size) throw new IndexOutOfBoundsException();
        return list[size-index-1];
    }

    public int pop() {
        if (size==0) throw new IndexOutOfBoundsException();
        int result = list[size-1];
        size--;
        return result;
    }

    public void push(int element) {
        size++;
        ensureCapacity(size);
        list[size-1] = element;
    }

    private void ensureCapacity(int capacity) {
        if (list.length < capacity) {
            list = Arrays.copyOf(list, capacity);
        }
    }

    public void rotateForward() {
       if (size < 2) return;
       int first = list[size-1];
       System.arraycopy(list, 0, list, 1, size-1);
       list[0] = first;
    }
    
    public void rotateBackward() {
        if (size < 2) return;
        int last = list[0];
        System.arraycopy(list, 1, list, 0, size-1);
        list[size-1] = last;
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }

    /**
     * Finds the target position to insert a value into the stack to keep it sorted in ascending order.
     * <br>The method works also if the stack is rotated.
     * <br>Calling this method on a stack that is not sorted or that contains duplicates returns an undefined result.
     * @param value the value to insert
     * @return the target position. Warning, if the value is greater than all values in the stack, the method returns 0, because the stack remains sorted but rotated.
     */
    public int findTargetPosition(int value) {
        int result = size + binarySearch(list, size, value, false) + 1;
        if (result == size) result = 0;
        return result;
    }

    public static int binarySearch(int[] list, int size, int value, boolean ascending) {
        if (size == 0) return -1;

        int head = getHeadIndex(list, size, ascending);
        int low = 0;
        int high = size - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            // Mappage de l'index virtuel vers l'index réel
            int actualMid = (head + mid) % size;
            int midVal = list[actualMid];

            if (midVal == value) return actualMid;

            boolean goRight = ascending ? (midVal < value) : (midVal > value);
            if (goRight) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        // 'low' est maintenant l'index virtuel d'insertion (0 à size)
        // On le convertit en index réel dans le tableau 'list'
        int actualInsertionIndex = (head + low) % size;
        
        return -(actualInsertionIndex + 1);
    }

    public int getHeadIndex() {
        return size - 1 - getHeadIndex(list, size, false);
    }

    private static int getHeadIndex(int[] list, int size, boolean ascending) {
        if (size <= 1) return 0;
        
        int low = 0;
        int high = size - 1;

        // Si le tableau n'est pas "brisé" (le dernier élément respecte l'ordre face au premier)
        if (ascending) {
            if (list[low] < list[high]) return 0;
        } else {
            if (list[low] > list[high]) return 0;
        }

        // Recherche dichotomique du pivot (le saut de continuité)
        while (low < high) {
            int mid = low + (high - low) / 2;
            boolean isBeforePivot = ascending ? (list[mid] >= list[0]) : (list[mid] <= list[0]);
            
            if (isBeforePivot) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    @Override
    public Iterator<Integer> iterator() {
        return Arrays.stream(toArray()).limit(size).iterator();
    }

    @Override
    public int[] toArray() {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = list[size-i-1];
        }
        return result;
    }
}
