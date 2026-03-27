package com.fathzer.pushswap.butterfly;

import java.util.BitSet;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.Stack;
import com.fathzer.pushswap.pusher.AbstractPusher;

class LisBackPusher extends AbstractPusher {
    private static final boolean ASSERT = true;
    private final BitSet inA;
    private final DelayedOperations delayed = new DelayedOperations(sorter);
    
    LisBackPusher(AbstractPushSwapSorter sorter, BitSet inA) {
        super(sorter);
        this.inA = inA;
        sorter.debug("inA is "+inA);
    }

    protected void pa() {
        delayed.pa();
        inA.set(sorter.getAStack().first());
    }
    
    @Override
    public void push() {
        Stack stackA = sorter.getAStack();
        Stack stackB = sorter.getBStack();
        // Now push other elements on top of A
        for (int target=this.inA.previousClearBit(stackA.size()+stackB.size()-1); target>=0; target--) {
//                sorter.debug("------------"+target+"-----------");
            if (inA.get(target)) {
                doTargetInA(target);
            } else {
                doTargetInB(stackB, target);
            }
        }
        delayed.processPending();
    }

    protected void doTargetInA(int target) {
        // sorter.debug("Is in A. head is = " + delayed.headOfA()+", end is "+delayed.tailOfA()+" - rraRequired="+delayed.rraRequired);
        if (delayed.tailOfA() == target) {
            // This value is already in A stack => move it to the top
            // sorter.debug("max in last A position => rra");
            delayed.rra();
        } else {
            checkTargetPositioninA(target);
        }
    }

    private void checkTargetPositioninA(int target) {
        if (ASSERT && delayed.tailOfA() != target && !(delayed.isSaPending() && sorter.getAStack().get(1) == target)) {
            // The target can be at the top of A stack because we pushed it earlier => nothing to do
            // or in second position because a sa is required after an early push
            // FIXME It missed the second test
            throwError("Error: target " + target + " is in A but not at tail of stack");
        }
    }

    protected void doTargetInB(Stack stackB, int target) {
        boolean saRequired = stackB.first() == target - 1 && stackB.last()!=target;
        if (saRequired) {
            //FIXME should be tested for every value found on the way to target!
            // We will have to make a sa after adding max
            pa();
        }
        
        // On optimise le chemin pour ramener 'target' en haut de B
        int pos = stackB.getIndex(target);
        if (pos<0) {
            throwError("Error unable to find position of " + target + " in B");
        }
        
        if (pos==0) {
            // Nothing to do, target is already at the top of B
        } else if (pos <= stackB.size() / 2) {
            //TODO the test does not uses pending rra to be sure best way is rb (should be relevant only when stackB is small)
            // Faster to make rb
            // We should be clever and check if a sb is enough for the last rb (it could save a rrb call later)
            for (int i=0; i<pos-1; i++) {
                delayed.rb();
            }
            // Check if sb is better than rb
            int nextToBePushed = getNextValueInBToBePushed(target);
            if (stackB.first() == nextToBePushed || stackB.getIndex(nextToBePushed)>stackB.size() / 2) {
                // If next number of B to be pushed is the current first or at the bottom of the list, sb is better
                delayed.sb();
            } else {
                delayed.rb();
            }
        } else {
            // Faster to make rrb
            while (stackB.get(0) != target) delayed.rrb();
        }
        pa();
        if (saRequired) {
            delayed.sa();
        }
//                sorter.debugStacks();
    }

    protected int getNextValueInBToBePushed(int target) {
        return inA.previousClearBit(target-1);
    }

    private void throwError(String message) {
        System.out.println("stackA: " + sorter.getAStack());
        System.out.println("stackB: " + sorter.getBStack());
        System.out.println("Ope: " + sorter.getOperations());
        throw new IllegalStateException(message);
    }
}