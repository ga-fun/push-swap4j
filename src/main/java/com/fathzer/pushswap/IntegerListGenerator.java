package com.fathzer.pushswap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class IntegerListGenerator {
    private static final Random RANDOM = new Random();
    
    /**
     * Generates a random list of integers.
     * @param size the size of the list
     * @return the list (it is guaranteed that no integer is repeated)
     */
    public int[] generate(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        
        int[] result = new int[size];
        
        // Generate random integers with no duplicates
        for (int i = 0; i < size; i++) {
            int randomInt;
            do {
                randomInt = RANDOM.nextInt();
            } while (contains(result, i, randomInt));
            result[i] = randomInt;
        }
        
        return result;
    }
    
    private boolean contains(int[] array, int length, int value) {
        for (int i = 0; i < length; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalizes an array of integers by mapping them to a continuous range starting from 0.
     * This is useful for reducing the range of values while preserving their relative order.
     * 
     * @param array the array to normalize
     * @return a new array with normalized values
     */
    public static int[] normalize(int[] array) {
        if (array == null || array.length == 0) {
            return array;
        }
        
        // Create a copy of the array to avoid modifying the original
        int[] result = array.clone();
        
        // Create pairs of (value, original index) to preserve order
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            pairs.add(new Pair(result[i], i));
        }
        
        // Sort by value
        pairs.sort(Comparator.comparingInt(p -> p.value));
        
        // Assign normalized values
        int normalizedValue = 0;
        for (int i = 0; i < pairs.size(); i++) {
            Pair current = pairs.get(i);
            result[current.index] = normalizedValue;
            
            // Only increment normalized value if the next value is different
            if (i < pairs.size() - 1) {
                Pair next = pairs.get(i + 1);
                if (next.value != current.value) {
                    normalizedValue++;
                }
            }
        }
        
        return result;
    }
    
    private static class Pair {
        int value;
        int index;
        
        Pair(int value, int index) {
            this.value = value;
            this.index = index;
        }
    }
}
