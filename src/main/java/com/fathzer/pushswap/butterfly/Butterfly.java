package com.fathzer.pushswap.butterfly;

import java.util.List;
import java.util.stream.IntStream;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.SmallStackSorter;
import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.Operation;
import com.fathzer.pushswap.pusher.AbstractPusher;

public class Butterfly extends AbstractButterfly {

    public Butterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    private static class BasicBPusher extends AbstractA2BButterFlyPusher {
        private final int keptCount;
        private final int maxPushedValue;
        
        public BasicBPusher(AbstractPushSwapSorter sorter, int keptCount) {
            super(sorter);
            this.keptCount = keptCount;
            IStack stackA = sorter.getAStack();
            this.maxPushedValue = stackA.size() - 1 - keptCount;
            // L'offset définit la largeur de la fenêtre glissante.
            // Coéfficients empirique (marche bien pour 500 éléments).
            int windowSize = (int) (Math.sqrt((double)stackA.size()-keptCount) * 1.47);
//            int windowSize = (int) (Math.sqrt((double)stackA.size()-keptCount) * 1.6);

            this.low = 0;
            this.high = low + windowSize;
            sorter.debug("Start with window size {} pushing {} elements", List.of(() -> windowSize, () -> stackA.size()-keptCount));
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

    protected AbstractPusher getFirstPhasePusher() {
        return new BasicBPusher(this, 7);
    }

    @Override
    protected void onFirstPhaseEnded() {
        int[] arr = IntegerListGenerator.normalize(stackA.toArray());
        int[] target = IntStream.range(0, stackA.size()).toArray();
        List<Operation> operations = new SmallStackSorter().solve(arr, target);
        // Appliquer les opérations
        for (Operation op : operations) {
            op.apply(this);
        }
        debug("End of phase 2 (sort A) with {} operations", List.of(() -> getOperations().size()));
        debug("Stack A: {}", List.of(() -> stackA));
    }

    @Override
    protected AbstractPusher getSecondPhasePusher() {
        return new BtoAOrderedBackPusher(this);
    }
}
