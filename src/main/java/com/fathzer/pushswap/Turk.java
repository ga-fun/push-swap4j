package com.fathzer.pushswap;

import com.fathzer.pushswap.pusher.AbstractPusher;
import com.fathzer.pushswap.pusher.BtoAcheapestElementPusher;
import com.fathzer.pushswap.pusher.PushAllBut3;

public class Turk extends AbstractTwoPhasesPushSwapSorter {
    public Turk(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    @Override
    protected AbstractPusher getFirstPhasePusher() {
        return new PushAllBut3(this, true);
    }

    @Override
    protected void onFirstPhaseEnded() {
        // Sort the remaining 3 elements in A
        // If there are less than 3 elements, nothing to do
        if (stackA.size() < 3) return;
        
        int top = stackA.get(0);
        int mid = stackA.get(1);
        int bot = stackA.get(2);
        
        if (top > mid && mid < bot && top < bot) {
            sa();
        } else if (top > mid && mid > bot) {
            sa();
        } else if (top > mid && mid < bot && top > bot) {
            // Already sorted but rotated
        } else if (top < mid && mid > bot && top < bot) {
            sa();
        } else if (top < mid && mid > bot && top > bot) {
            // Already sorted but rotated
        }        
        super.onFirstPhaseEnded();
    }

    @Override
    protected AbstractPusher getSecondPhasePusher() {
        return new BtoAcheapestElementPusher(this);
    }

    @Override
    protected void onSecondPhaseEnded() {
        finalRotation();
        super.onSecondPhaseEnded();
    }

    private void finalRotation() {
        // Trouver l'index du plus petit élément
        int minIndex = stackA.getHeadIndex();
        
        // Rotation pour mettre le plus petit en haut
        Rotation rotation = new Rotation();
        int aSize = stackA.size();
        if (minIndex <= aSize / 2) {
            debug("Final rotation: " + minIndex + " forward rotations");
            rotation.ra = minIndex;
        } else {
            debug("Final rotation: " + (aSize - minIndex) + " reverse rotations");
            rotation.rra = aSize - minIndex;
        }
        rotate(rotation);
    }
}
