package com.fathzer.pushswap;

import java.util.BitSet;

public class LisTurk extends Turk {
    public LisTurk(int[] numbers) {
        super(numbers);
    }
    
    @Override
    protected void pushToB() {
        // Get the Longest Increasing Subsequence in A stack
        BitSet toKeep = LIS.getCircular(stackA.toArray());

        if (isDebug()) {
            System.out.println("Longest Increasing Subsequence: " + toKeep);
        }

        // Push all elements that are not in the Longest Increasing Subsequence to B
        int targetSize = toKeep.cardinality();
        while (stackA.size()>targetSize) {
            if (!toKeep.get(stackA.get(0))) {
                pb();
            } else {
                rr();
            }
        }
    }
}
