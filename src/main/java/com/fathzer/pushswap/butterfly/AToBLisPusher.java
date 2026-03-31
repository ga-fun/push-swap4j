package com.fathzer.pushswap.butterfly;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.StreamSupport;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.IStack;

class AToBLisPusher extends AbstractA2BButterFlyPusher {
    protected final BitSet keepInA;
    protected final List<Integer> toBeMoved;
    protected final int windowSize;
    protected int lowIndex;

    public AToBLisPusher(AbstractPushSwapSorter sorter, AToBLisPusherConfig config) {
        super(sorter);
        IStack stackA = sorter.getAStack();
        this.keepInA = config.getLis(stackA);
        this.toBeMoved = new ArrayList<>(StreamSupport.stream(stackA.spliterator(), false).filter(value -> !keepInA.get(value)).sorted().toList());
        this.windowSize = config.windowSizeBuilder.apply(stackA.size() - keepInA.cardinality());
        sorter.debug("LIS ("+ keepInA.cardinality()+" elements): " + keepInA + ". Starting PushToB with "+this.windowSize+" elements window");
        this.lowIndex = 0;
        this.low = toBeMoved.get(lowIndex);
        this.high = toBeMoved.get(lowIndex + windowSize - 1);
    }

    @Override
    public Command evaluate(int value) {
        if (keepInA.get(value)) {
            return Command.KEEP;
        }
        return super.evaluate(value);
    }
    
    @Override
    protected void incrementLimits() {
        lowIndex++;
        updateLimits();
    }

    protected void updateLimits() {
        if (lowIndex < toBeMoved.size()) {
            low = toBeMoved.get(lowIndex);
            int highIndex = lowIndex + windowSize - 1;
            if (highIndex < toBeMoved.size()) {
                high = toBeMoved.get(highIndex);
            }
        }
    }

    @Override
    public boolean isNotEnded(IStack a) {
        return a.size() > keepInA.cardinality();
    }

    public BitSet getInA() {
        return this.keepInA;
    }
}