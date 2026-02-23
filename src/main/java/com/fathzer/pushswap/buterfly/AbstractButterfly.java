package com.fathzer.pushswap.buterfly;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.IntegerListGenerator;
import com.fathzer.pushswap.buterfly.BPusher.Command;

public abstract class AbstractButterfly extends AbstractPushSwapSorter {

    protected AbstractButterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    protected abstract BPusher getBPusher();

    @Override
    public void sort() {
        BPusher bPusher = getBPusher();

        pushToB(bPusher);
        debug("End of phase 1 (push to B) with "+getOperations().size()+" operations");
        debugStacks();

        onPushToBEnded();
        pushBackOrdered();
    }
    
    private boolean isBHeadAscending() {
    	return stackB.size()>1 && stackB.get(0)<stackB.get(1);
    }

    protected void pushToB(BPusher bPusher) {
        int rbRequired = 0;

        int debugNoPushCount = 0;
        
        // ÉTAPE 1 : Transfert de A vers B (Création du sablier)
        while (bPusher.isNotEnded(stackA)) {
            int value = stackA.get(0);
            Command command = bPusher.evaluate(value);
//            debugStacks();

            if (command == Command.TO_BOTTOM) {
                // Élément "petit" -> fond de B
                pb();
                // Do not execute rb immediately, try to group it with a future ra
                rbRequired++;
                debugNoPushCount = 0;
//                debug("  -> Push to bottom. rbRequired="+rbRequired);
            } else if (command == Command.TO_TOP) {
                // Élément "moyen" -> haut de B
                rbRequired = executeDelayedRb(rbRequired);
                pb();
//                debug("  -> Push to top");
                
                if (isBHeadAscending()) {
                    // I tried here to also swap A when the two elements at top of A.get(1)>A.get(0), but for an unknown reason it leads to worse results.   
                    sb();
                }
                debugNoPushCount = 0;
            } else {
                // Élément "grand" -> on le fait défiler
//                debug("  -> Ignore");
                if (rbRequired>0) {
                    rr();
                    rbRequired--;
                } else {
                    ra();
                }
                debugNoPushCount++;
                // Optionnel : on pourrait incrémenter très légèrement range ici 
                // pour éviter de tourner trop longtemps, mais le ra suffit 
                // car i finira par augmenter via les autres conditions.
            }
            if (debugNoPushCount>2*stackA.size()) {
                System.out.println("Breaking loop, too many no-push iterations");
                System.exit(-1);
            }
        }
    }

    /**
     * Called when the push to B phase is ended.
     * This is where you can add custom logic to, for instance, sort A before pushing back to B.
     */
    protected void onPushToBEnded() {
        // Hook method for subclasses
    }

    private int executeDelayedRb(int count) {
        for (int j = 0; j < count; j++) {
            rb();
        }
        return 0;
    }

    protected void pushBackOrdered() {
        // Retour de B vers A (Vidage du sablier)
        // On cherche toujours le plus grand élément restant dans B
        while (!stackB.isEmpty()) {
            int target = stackB.size() - 1; // On cherche l'index maximum actuel dans B
            int next = target - 1;

            if (stackB.first() == next && stackB.last()!=target) {
                pa();
                // On cherchera le max au tour d'après, puis on fera un SA
            }
            
            // On optimise le chemin pour ramener 'target' en haut de B
            int pos = findPositionInB(target);
            
            if (pos <= stackB.size() / 2) {
                // Should be clever to check if a sb is enough for the last rb (it could save a rrb call later)
                while (stackB.get(0) != target) rb();
            } else {
                while (stackB.get(0) != target) rrb();
            }
            
            pa();

            // If we first push a smaller value (ex: 498 then 499), we need to sort them
            if (stackA.size() > 1 && stackA.get(0) > stackA.get(1)) {
                // Should be clever to delay this in order to group with a potential sb operation
                sa();
            }
        }
    }

    // Méthode utilitaire pour trouver la position d'une valeur dans la pile B
    protected int findPositionInB(int indexValue) {
        return stackB.getIndex(indexValue);
    }
    
    protected void debug(String message) {
    	if (isDebug()) {
            System.out.println(message);
        }
    }
    
    protected void debugStacks() {
    	if (isDebug()) {
            debug("Stack A: "+stackA);
            debug("Stack B: "+stackB);
    	}
    }
}
