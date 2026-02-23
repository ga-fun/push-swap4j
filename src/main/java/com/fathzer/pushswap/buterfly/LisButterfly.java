package com.fathzer.pushswap.buterfly;

import java.util.BitSet;
import java.util.List;
import java.util.stream.StreamSupport;

import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.LIS;
import com.fathzer.pushswap.Rotation;

public class LisButterfly extends AbstractButterfly {
    private static final boolean USE_CIRCULAR_LIS = true;
    private BitSet inA;

    public LisButterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    private class LisPusher extends AbstractBPusher {
        private final List<Integer> toBeMoved;
        private final int windowSize;
        private int lowIndex;

        public LisPusher() {
            inA = USE_CIRCULAR_LIS ? LIS.getCircular(stackA.toArray()) : LIS.get(stackA.toArray(), 0);
            this.toBeMoved = StreamSupport.stream(stackA.spliterator(), false).filter(value -> !inA.get(value)).sorted().toList();
            // windowSize définit la largeur de la fenêtre glissante.
            // Coéfficients empirique (marche bien pour 500 éléments).
            this.windowSize = (int) (Math.sqrt((double)stackA.size()-inA.cardinality()) * 1.6);
            debug("LIS ("+ inA.cardinality()+" elements): " + inA + ". Starting PushToB with "+this.windowSize+" elements window");
            this.lowIndex = 0;
            this.low = toBeMoved.get(lowIndex);
            this.high = toBeMoved.get(lowIndex + windowSize - 1);
        }

        @Override
        public Command evaluate(int value) {
            if (inA.get(value)) {
                return Command.KEEP;
            }
            return super.evaluate(value);
        }
        
        @Override
        protected void incrementLimits() {
            lowIndex++;
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
            return stackA.size() > inA.cardinality();
        }
    }

    @Override
    protected BPusher getBPusher() {
        return new LisPusher();
    }

    private static class DelayedOperations {
        // Some operations on A can be delayed in order to find some operations on B that can be grouped with delayed ones.
        private final LisButterfly manager;
        private boolean saRequired;
        private int rraRequired;
        private boolean saRequiredAfterNextPa;
        
        private DelayedOperations(LisButterfly manager) {
            this.manager = manager;
        }

        int headOfA() {
            if (rraRequired==0) {
                return manager.stackA.get(0);
            }
            return manager.stackA.get(manager.stackA.size() - rraRequired);
        }

        int tailOfA() {
            return manager.stackA.get(manager.stackA.size() - 1 -rraRequired);
        }
        
        void rra() {
            doPendingSa();
            rraRequired++;
        }

        void pa() {
            // Perform pending operations before pushing from B to A
            processPending();
            manager.pa();
            manager.inA.set(manager.stackA.get(0));
            
            if (saRequiredAfterNextPa) {
                saRequired = true;
                saRequiredAfterNextPa = false;
            }
        }

        void sb() {
            if (saRequired) {
                manager.ss();
                saRequired = false;
            } else {
                manager.sb();
            }
        }

        void rb() {
            manager.rb();
        }

        void rrb() {
            if (rraRequired>0) {
                doPendingSa();
                rraRequired--;
                manager.rrr();
            } else {
                manager.rrb();
            }

        }

        void processPending() {
            doPendingSa();
            while (rraRequired > 0) {
                manager.rra();
                rraRequired--;
            }
        }

        private void doPendingSa() {
            if (saRequired) {
                manager.sa();
                saRequired = false;
            }
        }
    }

    @Override
    protected void pushBackOrdered() {
        // Insert highest missing element in A
        int aIndex = stackA.getHeadIndex();
        // Highest value of A is the one before head
        aIndex = aIndex==0 ? stackA.size()-1 : aIndex-1;
        int max = stackA.size() + stackB.size() - 1;
        while (stackA.get(aIndex) == max) {
            max--;
            aIndex = aIndex==0 ? stackA.size()-1 : aIndex-1;
        }
        int bIndex = stackB.getIndex(max);
        aIndex = aIndex==stackA.size()-1 ? 0 : aIndex+1;
        if (isDebug()) {
            System.out.println("Should insert: " + max + " at index " + aIndex + " of A from index " + bIndex +" of B");
        }
        // Ensure first element of A is max number in lists or the smallest element of A (in this case, highest value should be inserted at A's top)
        Rotation rotation = new Rotation();
        rotation.cheapest(stackB, bIndex, stackA, aIndex);
        this.rotate(rotation);
        if (isDebug()) {
            System.out.println("Opérations phase 1: (after "+ rotation.cost() + " cost) " + getOperations());
        }
        // Now push other elements on top of A
        DelayedOperations delayed = new DelayedOperations(this);
        for (int target=max; target>=0; target--) {
            debug("------------"+target+"-----------");
            if (inA.get(target)) {
                debug("Is in A. head is = " + delayed.headOfA()+", end is "+delayed.tailOfA()+" - rraRequired="+delayed.rraRequired);
                if (delayed.tailOfA() == target) {
                    // This value is already in A stack => move it to the top
                	debug("max in last A position => rra");
                    delayed.rra();
                } else if (delayed.headOfA() == target) {
                	debug("max in first A position: ignore");
                    // This value is at the top of A stack because we pushed it earlier => nothing to do
                    //TODO Warning will not work with delayed sa.
                }
                continue;
            }

            if (stackB.first() == target - 1 && stackB.last()!=target) {
                //FIXME should be tested for every value found on the way to target!
                delayed.pa();
                // We will have to make a sa after adding max
                delayed.saRequiredAfterNextPa = true;
            }
            
            // On optimise le chemin pour ramener 'target' en haut de B
            int pos = findPositionInB(target);
            if (pos<0) {
                throwError("Error unable to find position of " + target + " in B");
            }
            
            if (pos==0) {
                // Nothing to do, target is already at the top of B
            } else if (pos <= stackB.size() / 2) {
            	// FIXME, the test does not uses pending rra to be sure best way is rb (should be relevant only when stackB is small)
            	// Faster to make rb
                // We should be clever and check if a sb is enough for the last rb (it could save a rrb call later)
                for (int i=0; i<pos-1; i++) {
                    delayed.rb();
                }
                // Check if sb is better than rb
                int nextToBePushed = getNextValueInBToBePushed(target);
                boolean sbBetter = false;
                if (stackB.first() == nextToBePushed || findPositionInB(nextToBePushed)>stackB.size() / 2) {
                	// If next number of B to be pushed is the current first or at the bottom of the list, sb is better
                    delayed.sb();
                } else {
                    delayed.rb();
                }
            } else {
            	// Faster to make rrb
                while (stackB.get(0) != target) delayed.rrb();
            }
            
            delayed.pa();

            // // If we first push a smaller value (ex: 498 then 499), we need to sort them
            // if (saRequired) {
            //     // Should be clever to delay this in order to group with a potential sb operation
            //     sa();
            //     saRequired = false;
            // }
            debug("StackA: "+stackA);
            debug("StackB: "+stackB);
        }
        delayed.processPending();
    }
    
    private int getNextValueInBToBePushed(int target) {
        int nextValue = target-1;
        while (inA.get(nextValue)) {
            nextValue--;
        }
        return nextValue;
    }

    private void throwError(String message) {
        System.out.println("stackA: " + stackA);
        System.out.println("stackB: " + stackB);
        System.out.println("Ope: " + operations);
        throw new IllegalStateException(message);
    }
}
