package com.fathzer.pushswap.buterfly;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.AbstractTwoPhasesPushSwapSorter;
import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.pusher.AbstractPusher;

public abstract class AbstractButterfly extends AbstractTwoPhasesPushSwapSorter {

    protected AbstractButterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    public static class BtoAOrderedBackPusher extends AbstractPusher {
        public BtoAOrderedBackPusher(AbstractPushSwapSorter sorter) {
            super(sorter);
        }
        
        @Override
        public void push() {
            IStack stackA = sorter.getAStack();
            IStack stackB = sorter.getBStack();
            // Retour de B vers A (Vidage du sablier)
            // On cherche toujours le plus grand élément restant dans B
            while (!stackB.isEmpty()) {
                int target = stackB.size() - 1; // On cherche l'index maximum actuel dans B
                int next = target - 1;
                
                if (stackB.first() == next && stackB.last()!=target) {
                    sorter.pa();
                    // On cherchera le max au tour d'après, puis on fera un SA
                }
                
                // On optimise le chemin pour ramener 'target' en haut de B
                int pos = stackB.getIndex(target);
                
                if (pos <= stackB.size() / 2) {
                    // Should be clever to check if a sb is enough for the last rb (it could save a rrb call later)
                    while (stackB.get(0) != target) sorter.rb();
                } else {
                    while (stackB.get(0) != target) sorter.rrb();
                }
                
                sorter.pa();
    
                // If we first pushed a smaller value (ex: 498 then 499), we need to sort them
                if (stackA.size() > 1 && stackA.get(0) > stackA.get(1)) {
                    // Should be clever to delay this in order to group with a potential sb operation
                    sorter.sa();
                }
            }
        }
    }
}
