package com.fathzer.pushswap.pusher;

import java.util.function.IntConsumer;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.IStack;

/**
 * A very basic pusher that pushes all elements from one stack to another, except for the last 3 elements.
 */
public class PushAllBut3 extends AbstractPusher {
    private final IStack from;
    private final IntConsumer push;
    
    /**
     * Constructor.
     * @param sorter The sorter to use.
     * @param aToB True to push from A to B, false to push from B to A.
     */
    public PushAllBut3(AbstractPushSwapSorter sorter, boolean aToB) {
        super(sorter);
        this.from = sorter.getAStack();
        this.push = aToB ? sorter::pb : sorter::pa;
    }
    
    @Override
    public void push() {
        if (from.size() > 3) {
            push.accept(from.size() - 3);
        }
    }
}