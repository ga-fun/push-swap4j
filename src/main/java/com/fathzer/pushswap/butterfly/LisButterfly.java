package com.fathzer.pushswap.butterfly;

import java.util.List;

import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.Rotation;
import com.fathzer.pushswap.pusher.AbstractPusher;

public class LisButterfly extends AbstractButterfly {
    private static final boolean USE_CIRCULAR_LIS = true;
    protected final AToBLisPusherConfig config;
    protected AToBLisPusher lisPusher;

    public LisButterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
        this.config = new AToBLisPusherConfig();
        this.config.setCircularLis(USE_CIRCULAR_LIS);
    }

    @Override
    protected AbstractPusher getFirstPhasePusher() {
        this.lisPusher = new AToBLisPusher(this, this.config);
        return this.lisPusher;
    }

    @Override
    protected void onFirstPhaseEnded() {
        super.onFirstPhaseEnded();
        // The A stack is not in position where the next/highest element should be inserted
        // The challenge is to take advantage of the movements in A to push in any elements along the way. 
        // For example, if A contains 77, 80, 97, and 5, and B contains 95, 91, 93, 99, 98, and 96, 
        // pushing 95 between 80 and 97 adds two movements compared to the shortest possible positioning to insert 99 
        // (ra,ra,pa,rr,rr,pa versus rr,rr,rr,pa), but the sequence to insert 95 is three movements shorter 
        // (pa,rra,pa,rra versus pa,rra,pa,rrb,rrb,rrb,pa). Furthermore, since 94 is to the right of 95 in B, 
        // we'll gain even more by saving the movement through 91 and 93.
        // But it's a challenge for a small benefit!
        // So, for now, we will just rotate the stacks to be able to push the first element of B into A
        int aIndex = stackA.getHeadIndex();
        aIndex = Rotation.getPreviousIndex(stackA, aIndex);
        int max = stackA.size() + stackB.size() - 1;
        while (stackA.get(aIndex) == max) {
            max--;
            aIndex = Rotation.getPreviousIndex(stackA, aIndex);
        }
        int bIndex = stackB.getIndex(max);
        aIndex = Rotation.getNextIndex(stackA, aIndex);
        debug("Should insert: " + max + " at index " + aIndex + " of A from index " + bIndex +" of B");
        // Ensure first element of A is max number in lists or the smallest element of A (in this case, highest value should be inserted at A's top)
        Rotation rotation = new Rotation();
        rotation.cheapest(stackB, bIndex, stackA, aIndex);
        rotate(rotation);
        debug("Opérations phase 1: (after {} cost) {}", List.of(rotation::cost, this::getOperations));

    }

    @Override
    protected AbstractPusher getSecondPhasePusher() {
        return new LisBackPusher(this, this.lisPusher.getInA());
    }
}
