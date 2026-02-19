package com.fathzer.pushswap.buterfly;

import java.util.List;
import java.util.stream.IntStream;

import com.fathzer.pushswap.CleanKillingSmallStackSorter;
import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.Operation;

public class Butterfly extends AbstractButterfly {

    public Butterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    private class BasicBPusher extends AbstractBPusher {
        private final int keptCount;
        private final int maxPushedValue;
        
        public BasicBPusher(int keptCount) {
            this.keptCount = keptCount;
            this.maxPushedValue = stackA.size() - 1 - keptCount;
            this.low = 0;
            // L'offset définit la largeur de la fenêtre glissante.
            // Coéfficients empirique (marche bien pour 500 éléments).
            this.high = low + (int) (Math.sqrt(stackA.size()-keptCount) * 1.47);
            if (isDebug()) {
                System.out.println("Start with window size "+keptCount+" for "+stackA.size()+" elements");
            }
        }

        @Override
        protected void incrementLimits() {
            low++;
            high = Math.min(high + 1, maxPushedValue);
        }

        @Override
        public boolean isNotEnded(IStack a) {
            return a.size() > keptCount;
        }
    }

    protected BPusher getBPusher() {
        return new BasicBPusher(7);
    }

    @Override
    protected void onPushToBEnded() {
        int[] arr = IntegerListGenerator.normalize(stackA.toArray());
        int[] target = IntStream.range(0, stackA.size()).toArray();
        List<Operation> operations = new CleanKillingSmallStackSorter().solve(arr, target);
        // Appliquer les opérations
        for (Operation op : operations) {
            op.apply(this);
        }
        if (isDebug()) {
            System.out.println("End of phase 2 (sort A) with "+getOperations().size()+" operations");
            System.out.println("Stack A: "+stackA);
        }
    }
}
