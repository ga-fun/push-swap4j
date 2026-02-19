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

    public LisButterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    private class LisPusher extends AbstractBPusher {
        private final BitSet lis;
        private final List<Integer> toBeMoved;
        private final int windowSize;
        private int lowIndex;

        public LisPusher() {
            this.lis = USE_CIRCULAR_LIS ? LIS.getCircular(stackA.toArray()) : LIS.get(stackA.toArray(), 0);
            if (isDebug()) {
                System.out.println("LIS ("+ lis.cardinality()+" elements): " + lis);
            }
            this.toBeMoved = StreamSupport.stream(stackA.spliterator(), false).filter(value -> !lis.get(value)).sorted().toList();
            // windowSize définit la largeur de la fenêtre glissante.
            // Coéfficients empirique (marche bien pour 500 éléments).
            this.windowSize = (int) (Math.sqrt(stackA.size()-lis.cardinality()) * 1.47);
            this.lowIndex = 0;
            this.low = toBeMoved.get(lowIndex);
            this.high = toBeMoved.get(lowIndex + windowSize - 1);
        }

        @Override
        public Command evaluate(int value) {
            if (lis.get(value)) {
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
            return stackA.size() > lis.cardinality();
        }
    }

    @Override
    protected BPusher getBPusher() {
        return new LisPusher();
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
        boolean saRequired = false;
        for (int target=max; target>=0; target--) {
//            System.out.println("------------"+target+"-----------");
            if (stackA.get(stackA.size()-1) == target) {
                // This value is already in A stack => move it to the top
//            	System.out.println("max in last A position => rra");
                rra();
                continue;
            } else if (stackA.get(0) == target) {
//            	System.out.println("max in first A position: ignore");
                // This value is at the top of A stack because we pushed it earlier => nothing to do
                //TODO Warning will not work with delayed sa.
                continue;
            }

            if (stackB.get(0) == target - 1/* && stackB.size() > 1 */) {
                pa();
                // We will have to make a sa after adding max
                saRequired = true;
            }
            
            // On optimise le chemin pour ramener 'target' en haut de B
            int pos = findPositionInB(target);
            if (pos<0) {
            	throw new IllegalStateException();
            }
            
            if (pos <= stackB.size() / 2) {
                // Should be clever to check if a sb is enough for the last rb (it could save a rrb call later)
                while (stackB.get(0) != target) rb();
            } else {
                while (stackB.get(0) != target) rrb();
            }
            
            pa();

            // If we first push a smaller value (ex: 498 then 499), we need to sort them
            if (saRequired) {
                // Should be clever to delay this in order to group with a potential sb operation
                sa();
                saRequired = false;
            }
//            System.out.println("StackA: "+stackA);
//            System.out.println("StackB: "+stackB);
        }
    }
}
