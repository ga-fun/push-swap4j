package com.fathzer.pushswap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class IntegerListGeneratorTest {
    
    private IntegerListGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new IntegerListGenerator();
    }
    
    @Test
    void test() {
        assertEquals(10, generator.generate(10).length, "Generated array should have the requested size");
        assertEquals(0, generator.generate(0).length, "Generated array should be empty for size 0");
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(-1);
        }, "Should throw IllegalArgumentException for negative size");

        int[] array = generator.generate(10);
        System.out.println(Arrays.toString(array));
        System.out.println(Arrays.toString(generator.normalize(array)));
    }
    
    @Test
    void testGenerateUniqueElements() {
        int size = 100;
        int[] result = generator.generate(size);
        
        Set<Integer> uniqueElements = new HashSet<>();
        for (int value : result) {
            assertTrue(uniqueElements.add(value), "All elements should be unique");
        }
        
        assertEquals(size, uniqueElements.size(), "Should have exactly " + size + " unique elements");
    }
    
    @Test
    void testGenerateRandomDistribution() {
        int size = 10000;
        int[] result = generator.generate(size);
        
        // Test bit distribution - each bit should be roughly 50% set and 50% unset
        int[] bitCounts = new int[32];
        
        for (int value : result) {
            for (int bit = 0; bit < 32; bit++) {
                if ((value & (1 << bit)) != 0) {
                    bitCounts[bit]++;
                }
            }
        }
        
        // Each bit should be set approximately 50% of the time
        // Allow some tolerance for statistical variation (±5%)
        int expectedCount = size / 2;
        int tolerance = size / 20;
        
        for (int bit = 0; bit < 32; bit++) {
            int actualCount = bitCounts[bit];
            assertTrue(Math.abs(actualCount - expectedCount) <= tolerance,
                      "Bit " + bit + " should be set approximately 50% of time. Expected: " + 
                      expectedCount + ", Actual: " + actualCount);
        }
    }
    
    @Test
    void testGenerateMultipleCalls() {
        int size = 50;
        int[] result1 = generator.generate(size);
        int[] result2 = generator.generate(size);
        
        // Results should be different (very high probability with random generation)
        assertNotEquals(Arrays.toString(result1), Arrays.toString(result2), 
                       "Multiple calls should generate different arrays");
    }
    
    @Test
    void testNormalizeLimitCases() {
        assertNull(generator.normalize(null), "Should return null for null input");
        assertEquals(0, generator.normalize(new int[]{}).length, "Should return empty array for empty input");

        int[] input = {0};
        int[] result = generator.normalize(input);
        assertArrayEquals(new int[]{0}, result, "Single element should be normalized to 0");
        assertNotSame(input, result, "Should return a new array instance");
    }
    
    @Test
    void testNormalize() {
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, generator.normalize(new int[]{1, 2, 3, 4, 5}), "Sorted array should map to 0,1,2,3,4");
        assertArrayEquals(new int[]{4, 1, 3, 0, 2}, generator.normalize(new int[]{10, 2, 8, 1, 5}), "Should preserve order while normalizing values");
        assertArrayEquals(new int[]{2, 1, 2, 0, 1}, generator.normalize(new int[]{5, 2, 5, 1, 2}), "Duplicates should map to same normalized value");
        assertArrayEquals(new int[]{1, 2, 0, 3}, generator.normalize(new int[]{-5, -2, -8, -1}), "Negative numbers should be normalized correctly");
        assertArrayEquals(new int[]{0, 2, 4, 1, 3}, generator.normalize(new int[]{-3, 0, 5, -1, 2}), "Mixed numbers should be normalized correctly");
    }
    
    @Test
    void testNormalizeOriginalUnmodified() {
        int[] input = {10, 5, 15};
        int[] original = input.clone();
        generator.normalize(input);
        
        assertArrayEquals(original, input, "Original array should not be modified");
    }
}
