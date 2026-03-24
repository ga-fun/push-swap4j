package com.fathzer.pushswap.pusher;

import java.util.BitSet;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.LIS;

public class AtoBAllButLisPusher extends AbstractPusher {
    private boolean circular = true;
    
    public AtoBAllButLisPusher(AbstractPushSwapSorter sorter) {
        super(sorter);
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }
    
    @Override
    public void push() {
        // Get the Longest Increasing Subsequence in A stack
        int[] array = sorter.getAStack().toArray();
        BitSet toKeep = circular ? LIS.getCircular(array) : LIS.get(array, 0);

        sorter.debug("Longest Increasing Subsequence: " + toKeep);

        // Push all elements that are not in the Longest Increasing Subsequence to B
        int targetSize = toKeep.cardinality();
        while (sorter.getAStack().size()>targetSize) {
            if (!toKeep.get(sorter.getAStack().first())) {
                sorter.pb();
            } else {
                sorter.ra();
            }
        }
    }
}