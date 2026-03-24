package com.fathzer.pushswap.pusher;

import com.fathzer.pushswap.AbstractPushSwapSorter;

/** An abstract class for pushers that push elements from one stack to another.
 * <br>Of course, a pusher that pushes from A to B can also, on certain conditions, push from B to A too
 * depending on its strategy.
 */
public abstract class AbstractPusher {
    protected final AbstractPushSwapSorter sorter;
    
    protected AbstractPusher(AbstractPushSwapSorter sorter) {
        this.sorter = sorter;
    }
    
    public abstract void push();
}
