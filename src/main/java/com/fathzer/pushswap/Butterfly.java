package com.fathzer.pushswap;

public class Butterfly extends AbstractPushSwapSorter {

    public Butterfly(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    @Override
    public void sort() {
        int n = stackA.size();
        // L'offset définit la largeur de la fenêtre glissante.
        // Pour 100, ~15-20 est bon. Pour 500, ~35-45 est idéal.
        int offset = (int) (Math.sqrt(n) * 1.8); 
        if (isDebug()) {
            System.out.println("Start with window size "+offset+" for "+n+" elements");
        }

        pushToB(offset, 3);
        sortThree();
        if (isDebug()) {
            System.out.println("End of phase 1 with "+getOperations().size()+" operations");
            System.out.println("Stack B: "+stackB);
        }

        pushBackOrdered();
    }

    private void pushToB(int range, int keptCount) {
        int i = 0;
        int maxPushedValue = stackA.size() - 1 - keptCount;
        // ÉTAPE 1 : Transfert de A vers B (Création du sablier)
        while (stackA.size() > keptCount) {
            int value = stackA.get(0);

            if (value <= i) {
                // Élément "petit" -> fond de B
                pb();
                rb();
                i++;
            } else if (value <= i + range && value <= maxPushedValue) {
                // Élément "moyen" -> haut de B
                pb();
                if (stackB.size() > 1 && stackB.get(0) < stackB.get(1)) {
                    sb();
                }
                i++;
            } else {
                // Élément "grand" -> on le fait défiler
                ra();
                // Optionnel : on pourrait incrémenter très légèrement l'offset ici 
                // pour éviter de tourner trop longtemps, mais le ra suffit 
                // car i finira par augmenter via les autres conditions.
            }
        }
    }

    private void sortThree() {
        if (stackA.size() != 3) return;
        
        int top = stackA.get(0);
        int mid = stackA.get(1);
        int bot = stackA.get(2);
        
        if (top > mid && mid < bot && top < bot) {
            sa();
        } else if (top > mid && mid > bot) {
            sa();
            rra();
        } else if (top > mid && mid < bot && top > bot) {
            ra();
        } else if (top < mid && mid > bot && top < bot) {
            sa();
            ra();
        } else if (top < mid && mid > bot && top > bot) {
            rra();
        }
    }

    private void pushBackOrdered() {
        // ÉTAPE 2 : Retour de B vers A (Vidage du sablier)
        // On cherche toujours le plus grand élément restant dans B
        while (!stackB.isEmpty()) {
            int target = stackB.size() - 1; // On cherche l'index maximum actuel dans B
            int next = target - 1;

            if (stackB.get(0) == next && stackB.size() > 1) {
                pa();
                // On cherchera le max au tour d'après, puis on fera un SA
            }
            
            // On optimise le chemin pour ramener 'target' en haut de B
            int pos = findPositionInB(target);
            
            if (pos <= stackB.size() / 2) {
                while (stackB.get(0) != target) rb();
            } else {
                while (stackB.get(0) != target) rrb();
            }
            
            pa();

            // Si on a poussé dans le désordre (ex: 498 puis 499)
            if (stackA.size() > 1 && stackA.get(0) > stackA.get(1)) {
                sa();
            }
        }
    }

    // Méthode utilitaire pour trouver la position d'un index dans la pile B
    private int findPositionInB(int indexValue) {
        for (int i=0; i<stackB.size(); i++) {
            if (stackB.get(i) == indexValue) {
                return i;
            }
        }
        return -1;
    }
}
