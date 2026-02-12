package com.fathzer.pushswap;

public class FastStack implements IStack {
    private long data; // La pile stockée sous forme de 4-bits segments
    private int size;

    public FastStack() {
        this.data = 0;
        this.size = 0;
    }

    public FastStack(IStack other) {
        this.data = from(other);
        this.size = other.size();
    }

    public static long from(IStack other) {
        long data = 0;
        for (int i = other.size() - 1; i >= 0; i--) {
            data = (data << 4) | (other.get(i) & 0xF);
        }
        return data;
    }

    public static long from(int[] array) {
        long data = 0;
        for (int i = array.length - 1; i >= 0; i--) {
            data = (data << 4) | (array[i] & 0xF);
        }
        return data;
    }

    public static long push(long data, int value) {
        return (data << 4) | (value & 0xF);
    }

    public static int peek(long data) {
        return (int) (data & 0xF);
    }

    public static int get(long data, int index) {
        return (int) ((data >> (index * 4)) & 0xF);
    }

    public static String toString(long data, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(get(data, i));
        }
        sb.append("]");
        return sb.toString();
    }

    public static long deleteHead(long data) {
        return data >> 4;
    }

    public static long swap(long data) {
        long v0 = data & 0xF;
        long v1 = (data >> 4) & 0xF;
        return (data & ~0xFFL) | (v0 << 4) | v1;
    }

    public static long rotateForward(long data, int size) {
        long v0 = data & 0xF;
        return (data >> 4) | (v0 << ((size - 1) * 4));
    }

    public static long rotateBackward(long data, int size) {
        int shift = (size - 1) * 4;
        long bottom = (data >> shift) & 0xF;
        // On retire le fond
        data &= ~(0xFL << shift);
        // On décale tout vers le haut et on insère le fond au sommet
        return (data << 4) | bottom;
    }

    @Override
    public void push(int value) {
        // Ajoute la valeur au sommet (les 4 bits de poids faible)
        data = push(data, value);
        size++;
    }

    @Override
    public int pop() {
        if (size == 0) throw new IllegalStateException("Stack is empty");
        int value = peek(data);
        data = deleteHead(data);
        size--;
        return value;
    }

    @Override
    public int get(int index) {
        if (index<0) throw new IndexOutOfBoundsException("Index cannot be negative");
        if (size <= index) throw new IndexOutOfBoundsException("Index out of bounds");
        // index 0 est le sommet. On décale de (index * 4) bits vers la droite
        return get(data, index);
    }

    @Override
    public void swap() {
        if (size < 2) return;
        data = swap(data);
    }

    @Override
    public void rotateForward() {
        if (size < 2) return;
        data = rotateForward(data, size);
    }

    @Override
    public void rotateBackward() {
        if (size < 2) return;
        data = rotateBackward(data, size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FastStack)) return false;
        FastStack other = (FastStack) o;
        return this.data == other.data && this.size == other.size;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(data) ^ size;
    }

    long getData() {
        return data;
    }
}