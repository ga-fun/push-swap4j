package com.fathzer.pushswap.buterfly;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.pusher.AbstractPusher;

public abstract class AbstractButterFlyBPusher extends AbstractPusher {
    protected int low;
    protected int high;

    enum Command {
        TO_BOTTOM,
        TO_TOP,
        KEEP
    }

    protected AbstractButterFlyBPusher(AbstractPushSwapSorter sorter) {
        super(sorter);
    }

    public Command evaluate(int value) {
        if (value<low) {
            // Élément "petit" -> fond de B
            incrementLimits();
            return Command.TO_BOTTOM;
        } else if (value <= high) {
            // Élément "moyen" -> haut de B
            incrementLimits();
            return Command.TO_TOP;
        } else {
            // Élément "grand" -> on le garde en A
            return Command.KEEP;
            // Optionnel : on pourrait incrémenter très légèrement high ici 
            // pour éviter de tourner trop longtemps, mais le ra suffit 
            // car i finira par augmenter via les autres conditions.            
        }
    }

    protected abstract void incrementLimits();

    protected abstract boolean isNotEnded(IStack a);

    @Override
    public void push() {
        int rbRequired = 0;

        int debugNoPushCount = 0;
        final IStack stackA = sorter.getAStack();
        
        // ÉTAPE 1 : Transfert de A vers B (Création du sablier)
        while (isNotEnded(stackA)) {
            int value = stackA.get(0);
            Command command = evaluate(value);
//            debugStacks();

            if (command == Command.TO_BOTTOM) {
                // Élément "petit" -> fond de B
                sorter.pb();
                // Do not execute rb immediately, try to group it with a future ra
                rbRequired++;
                debugNoPushCount = 0;
//                debug("  -> Push to bottom. rbRequired="+rbRequired);
            } else if (command == Command.TO_TOP) {
                // Élément "moyen" -> haut de B
                for (int j = 0; j < rbRequired; j++) {
                    sorter.rb();
                }
                rbRequired = 0;
                sorter.pb();
//                debug("  -> Push to top");
                
                if (isHeadAscending(sorter.getBStack())) {
                    // I tried here to also swap A when the two elements at top of A.get(1)>A.get(0), but for an unknown reason it leads to worse results.   
                    sorter.sb();
                }
                debugNoPushCount = 0;
            } else {
                // Élément "grand" -> on le fait défiler
//                debug("  -> Ignore");
                if (rbRequired>0) {
                    sorter.rr();
                    rbRequired--;
                } else {
                    sorter.ra();
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

    protected boolean isHeadAscending(IStack stack) {
        return stack.size()>1 && stack.get(0)<stack.get(1);
    }
}
