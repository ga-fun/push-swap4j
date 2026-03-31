package com.fathzer.pushswap.pusher;

import java.util.List;

import com.fathzer.pushswap.AbstractPushSwapSorter;
import com.fathzer.pushswap.Rotation;
import com.fathzer.pushswap.Stack;

/**
 * A pusher that pushes the cheapest element from B to A, preserving the order of elements in A (smallest value first, circular).
 */
public class BtoAcheapestElementPusher extends AbstractPusher {
    public BtoAcheapestElementPusher(AbstractPushSwapSorter sorter) {
        super(sorter);
    }

    @Override
    public void push() {
        Rotation rotation = new Rotation();
        while (!sorter.getBStack().isEmpty()) {
            pushCheapestToA(rotation);
        }
    }

    private void pushCheapestToA(Rotation rotation) {
        int cheapestIndex = -1;
        int cheapestCost = Integer.MAX_VALUE;
        int cheapestTargetIndex = -1;
        int cheapestValue = 0;

        final Stack aStack = sorter.getAStack();
        final Stack bStack = sorter.getBStack();

        boolean tied = false;
        // Parcourir tous les éléments de B
        for (int i = 0; i < bStack.size(); i++) {
            int value = bStack.get(i);
            int targetIndex = aStack.findTargetPosition(value);

            rotation.cheapest(bStack, i, aStack, targetIndex);
            int cost = rotation.cost();
            if (cost < cheapestCost) {
                cheapestIndex = i;
                cheapestTargetIndex = targetIndex;
                cheapestValue = value;
                cheapestCost = cost;
                tied = false;
            } else if (cost == cheapestCost) {
                tied = true;
                if (value>cheapestValue) {
                    cheapestIndex = i;
                    cheapestTargetIndex = targetIndex;
                    cheapestValue = value;
                }
            }
        }
        
        // Exécuter les mouvements pour l'élément le moins cher
        rotation.cheapest(bStack, cheapestIndex, aStack, cheapestTargetIndex);
        sorter.debug("Phase 2: Push cheapest to A: " + cheapestValue + " at index " + cheapestIndex+ " with cost " + cheapestCost+ " ("+tied+") with rotation "+rotation);
        sorter.rotate(rotation);
        // Pousser dans A
        sorter.pa();
        sorter.debug(" => Stacks: {} {}", List.of(() -> aStack, () -> bStack));
    }
}