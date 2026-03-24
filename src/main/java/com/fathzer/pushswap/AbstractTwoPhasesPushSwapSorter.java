package com.fathzer.pushswap;

import com.fathzer.pushswap.pusher.AbstractPusher;

/** A generic sorter that uses two passes to sort the stack.
 *  <ul>
 *   <li>First pass: push elements to B</li>
 *   <li>Second pass: push elements back to A</li>
 *  </ul>
*/
public abstract class AbstractTwoPhasesPushSwapSorter extends AbstractPushSwapSorter {

    protected AbstractTwoPhasesPushSwapSorter(int[] numbers) {
        super(numbers);
    }

    protected abstract AbstractPusher getFirstPhasePusher();
    protected abstract AbstractPusher getSecondPhasePusher();


    @Override
    public void sort() {
        if (isSorted()) return;

        AbstractPusher firstPhasePusher = getFirstPhasePusher();
        firstPhasePusher.push();
        onFirstPhaseEnded();

        AbstractPusher secondPhasePusher = getSecondPhasePusher();
        secondPhasePusher.push();
        onSecondPhaseEnded();
    }

    /**
     * Called when the first phase is ended.
     * <br>This method does nothing except output debug information.
     * <br>You can add custom logic here.
     */
    protected void onFirstPhaseEnded() {
        debug("End of phase 1 with "+getOperations().size()+" operations");
        debugStacks();
    }

    /**
     * Called when the second phase is ended.
     * <br>This method does nothing except output debug information.
     * <br>You can add custom logic here.
     */
    protected void onSecondPhaseEnded() {
        debug("End of phase 2 with "+getOperations().size()+" operations");
    }
}
