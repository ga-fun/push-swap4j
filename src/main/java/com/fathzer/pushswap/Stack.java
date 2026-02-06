package com.fathzer.pushswap;

import java.util.Arrays;
import java.util.Iterator;

public class Stack implements Iterable<Integer> {
    private int[] list;  // Index 0 = haut de la pile
    private int size;

    public Stack() {
        this(0);
    }

    public Stack(int intialCapacity) {
        this.list = new int[intialCapacity];
        this.size = 0;
    }

    public Stack(int[] elements) {
        this.list = new int[elements.length];
        System.arraycopy(elements, 0, this.list, 0, elements.length);
        this.size = elements.length;
    }

    public boolean isEmpty() {
        return size==0;
    }
    
    public int size() {
        return size;
    }
    
    public int get(int index) {
        if (index>=size) throw new IndexOutOfBoundsException();
        return list[index];
    }

    public int pop() {
        if (size==0) throw new IndexOutOfBoundsException();
        int result = list[0];
        size--;
        System.arraycopy(list, 1, list, 0, size);
        return result;
    }

    public void push(int element) {
        size++;
        ensureCapacity(size);
        System.arraycopy(list, 0, list, 1, size-1);
        list[0] = element;
    }

    public void push(Stack other, int count) {
        size += count;
        ensureCapacity(size);
        System.arraycopy(list, 0, list, count, size-count);
        for (int i = 1; i <= count; i++) {
            list[count - i] = other.get(i-1);
        }
        other.delete(count);
    }

    private void delete(int count) {
        size -= count;
        System.arraycopy(list, count, list, 0, size);
    }

    private void ensureCapacity(int capacity) {
        if (list.length < capacity) {
            list = Arrays.copyOf(list, capacity);
        }
    }

    public void rotateForward() {
       if (size < 2) return;
       int first = list[0];
       System.arraycopy(list, 1, list, 0, size-1);
       list[size-1] = first;
    }

    public void rotateForward(int count) {
        if (size < 2) return;
        int k = count % size;
        if (k == 0) return;

        reverse(0, k - 1); // Inverse le bloc de tête
        reverse(k, size - 1); // Inverse le bloc de queue
        reverse(0, size - 1); // Inverse tout
    }

    public void rotateBackward() {
        if (size < 2) return;
        int last = list[size-1];
        System.arraycopy(list, 0, list, 1, size-1);
        list[0] = last;
    }

    public void rotateBackward(int count) {
        if (size < 2) return;
        int k = count % size;
        if (k == 0) return;

        reverse(0, size - k - 1); // Inverse le bloc de tête (ce qui reste)
        reverse(size - k, size - 1); // Inverse le bloc de queue (ce qui va monter)
        reverse(0, size - 1); // Inverse tout
    }

    private void reverse(int start, int end) {
        while (start < end) {
            int temp = list[start];
            list[start] = list[end];
            list[end] = temp;
            start++;
            end--;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(list[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
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
     * <br>Calling this method on a stack that is not sorted or that contains duplicates or with a value that is already in the stack returns an undefined result.
     * @param value the value to insert
     * @return the target position. Warning, if the value is greater than all values in the stack, the method returns 0, because the stack remains sorted but rotated.
     */
    public int findTargetPosition(int value) {
        int result = -(binarySearch(value, true)+1);
        if (result==size) result=0;
        return result;
    }

    private int binarySearch(int value, boolean ascending) {
        if (size == 0) return -1;

        int head = getHeadIndex(ascending);
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
        return getHeadIndex(true);
    }

    private int getHeadIndex(boolean ascending) {
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
        return Arrays.stream(list).limit(size).iterator();
    }

    public int[] toArray() {
        if (size==list.length) return list;
        var result = new int[size];
        System.arraycopy(list, 0, result, 0, size);
        return result;
    }
}
