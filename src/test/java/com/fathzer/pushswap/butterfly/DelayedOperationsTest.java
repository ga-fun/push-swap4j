package com.fathzer.pushswap.butterfly;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.pushswap.Operation.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.butterfly.DelayedOperations;

class DelayedOperationsTest {
    private TestSorter sorter;
    private DelayedOperations delayedOps;
    private TestSorter expected;
    
    @BeforeEach
    void setUp() {
        sorter = buildSorter();
        delayedOps = new DelayedOperations(sorter);
        expected = buildSorter();
    }

    private TestSorter buildSorter() {
        TestSorter result = new TestSorter(new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        result.pb();
        result.pb();
        result.pb();
        result.getOperations().clear();
        return result;
    }

    private void assertVirtualEquals() {
        assertArrayEquals(expected.getBStack().toArray(), sorter.getBStack().toArray(), "Stack B are different");
        assertEquals(expected.getAStack().size(), sorter.getAStack().size(), "Stack A lengths are different");
        for (int i = 0; i < expected.getAStack().size(); i++) {
            assertEquals(expected.getAStack().get(i), delayedOps.elementOfA(i), "Stack A elements "+i+" are different");
        }
        assertEquals(expected.getAStack().first(), delayedOps.headOfA(), "headOfA != A.first()");
        assertEquals(expected.getAStack().last(), delayedOps.tailOfA(), "tailOfA != A.last()");
    }
    
    @Test
    void testInitialState() {
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
        assertTrue(sorter.getOperations().isEmpty());
    }
    
    @Test
    void testSaDelaying() {
        delayedOps.sa();
        expected.sa();
        assertTrue(delayedOps.isSaPending());
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testSaWithSbCombination() {
        delayedOps.sa();
        expected.sa();
        delayedOps.sb();
        expected.sb();
        
        assertFalse(delayedOps.isSaPending());
        assertEquals(List.of(SS), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testSbWithoutSa() {
        delayedOps.sb();
        expected.sb();
        assertEquals(List.of(SB), sorter.getOperations());
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
    }
    
    @Test
    void testRaWithoutPendingSa() {
        delayedOps.ra();
        expected.ra();
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testRaWithPendingSa() {
        delayedOps.sa();
        expected.sa();
        delayedOps.ra();
        expected.ra();
        
        assertFalse(delayedOps.isSaPending());
        assertEquals(List.of(SA), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testRraWithoutPendingSa() {
        delayedOps.rra();
        expected.rra();
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testRraWithPendingSa() {
        delayedOps.sa();
        expected.sa();
        delayedOps.rra();
        expected.rra();
        
        assertFalse(delayedOps.isSaPending());
        assertEquals(List.of(SA), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testMultipleRaOperations() {
        delayedOps.ra();
        expected.ra();
        delayedOps.ra();
        expected.ra();
        delayedOps.ra();
        expected.ra();
        
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testMultipleRraOperations() {
        delayedOps.rra();
        expected.rra();
        delayedOps.rra();
        expected.rra();
        delayedOps.rra();
        expected.rra();
        
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testMixedRaRraOperations() {
        delayedOps.ra();
        expected.ra();
        delayedOps.ra();
        expected.ra();
        delayedOps.rra();
        expected.rra();
        
        assertTrue(sorter.getOperations().isEmpty());
        assertVirtualEquals();
    }
    
    @Test
    void testRbWithNegativeRraRequired() {
        delayedOps.ra();
        expected.ra();
        delayedOps.rb();
        expected.rb();
        
        assertEquals(List.of(RR), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testRbWithoutNegativeRraRequired() {
        delayedOps.rra();
        expected.rra();
        delayedOps.rb();
        expected.rb();
        
        assertEquals(List.of(RB), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testRrbWithPositiveRraRequired() {
        delayedOps.rra();
        expected.rra();
        delayedOps.rrb();
        expected.rrb();
        
        assertEquals(List.of(RRR), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testRrbWithoutPositiveRraRequired() {
        delayedOps.ra();
        expected.ra();
        delayedOps.rrb();
        expected.rrb();
        
        assertEquals(List.of(RRB), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testPaWithPendingOperations() {
        delayedOps.sa();
        expected.sa();
        delayedOps.ra();
        expected.ra();
        delayedOps.pa();
        expected.pa();
        
        assertEquals(List.of(SA, RA, PA), sorter.getOperations());
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
    }
    
    @Test
    void testProcessPendingWithSa() {
        delayedOps.sa();
        expected.sa();
        delayedOps.processPending();
        
        assertEquals(List.of(SA), sorter.getOperations());
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
    }
    
    @Test
    void testProcessPendingWithRotations() {
        delayedOps.ra();
        expected.ra();
        delayedOps.rb();
        expected.rb();
        delayedOps.ra();
        expected.ra();
        delayedOps.processPending();
        
        assertEquals(List.of(RR, RA), sorter.getOperations());
        assertVirtualEquals();
    }
    
    @Test
    void testProcessPendingWithBoth() {
        delayedOps.sa();
        expected.sa();
        delayedOps.ra();
        expected.ra();
        delayedOps.rra();
        expected.rra();
        delayedOps.processPending();
        
        assertEquals(List.of(SA), sorter.getOperations());
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
    }
    
    @Test
    void testComplexScenario() {
        delayedOps.sa();
        expected.sa();
        delayedOps.ra();
        expected.ra();
        delayedOps.rb();
        expected.rb();
        delayedOps.sa();
        expected.sa();
        delayedOps.sb();
        expected.sb();
        delayedOps.rra();
        expected.rra();
        delayedOps.sa();
        expected.sa();
        delayedOps.rb();
        expected.rb();
        delayedOps.processPending();
        
        assertEquals(List.of(SA, RR, SS, RRA, RB, SA), sorter.getOperations());
        assertFalse(delayedOps.isSaPending());
        assertVirtualEquals();
    }
    
    @Test
    void testSingleElementStack() {
        TestSorter singleSorter = new TestSorter(new int[]{1});
        DelayedOperations singleDelayed = new DelayedOperations(singleSorter);
        
        assertEquals(1, singleDelayed.headOfA());
        assertEquals(1, singleDelayed.tailOfA());
    }
    
    @Test
    void testTwoElementStack() {
        TestSorter twoSorter = new TestSorter(new int[]{1, 2});
        DelayedOperations twoDelayed = new DelayedOperations(twoSorter);
        
        assertEquals(1, twoDelayed.headOfA());
        assertEquals(2, twoDelayed.tailOfA());
        
        twoDelayed.ra();
        assertEquals(2, twoDelayed.headOfA());
        assertEquals(1, twoDelayed.tailOfA());
        
        twoDelayed.rra();
        assertEquals(1, twoDelayed.headOfA());
        assertEquals(2, twoDelayed.tailOfA());

        twoDelayed.sa();
        assertEquals(2, twoDelayed.headOfA());
        assertEquals(1, twoDelayed.tailOfA());
    }
    
    private static class TestSorter extends AbstractPushSwapSorter {
        
        public TestSorter(int[] numbers) {
            super(numbers);
        }
        
        @Override
        public void sort() {
            // Empty implementation - TestSorter is only used for testing DelayedOperations,
            // not for actual sorting functionality
        }
    }
}
