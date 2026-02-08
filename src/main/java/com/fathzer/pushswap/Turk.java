package com.fathzer.pushswap;

public class Turk extends PushSwapSorter {
    public Turk(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    @Override
    public void sort() {
        if (isSorted()) return;

        pushToB();
        if (isDebug()) {
            System.out.println("End of phase 1:");
            printStacks();
        }

        // Phase 2 : Ramener les éléments de B vers A de manière optimale
        Rotation rotation = new Rotation();
        while (!stackB.isEmpty()) {
            pushCheapestToA(rotation);
        }
        
        // Rotation finale pour avoir le plus petit en haut
        finalRotation(rotation);
    }

    protected void pushToB() {
        // Phase 1 : Pousser tout dans B sauf 3 éléments
        if (stackA.size() > 3) {
            pb(stackA.size() - 3);
        }
        
        // Trier les 3 éléments restants dans A
        if (stackA.size() != 3) return;
        
        int top = stackA.get(0);
        int mid = stackA.get(1);
        int bot = stackA.get(2);
        
        if (top > mid && mid < bot && top < bot) {
            sa();
        } else if (top > mid && mid > bot) {
            sa();
        } else if (top > mid && mid < bot && top > bot) {
            // Already sorted but rotated
        } else if (top < mid && mid > bot && top < bot) {
            sa();
        } else if (top < mid && mid > bot && top > bot) {
            // Already sorted but rotated
        }
    }
    
    private void pushCheapestToA(Rotation rotation) {
        int cheapestIndex = -1;
        int cheapestCost = Integer.MAX_VALUE;
        int cheapestTargetIndex = -1;
        int cheapestValue = 0;

        boolean tied = false;
        // Parcourir tous les éléments de B
        for (int i = 0; i < stackB.size(); i++) {
            int value = stackB.get(i);
            int targetIndex = stackA.findTargetPosition(value);

            rotation.cheapest(stackB, i, stackA, targetIndex);
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
        rotation.cheapest(stackB, cheapestIndex, stackA, cheapestTargetIndex);
        if (isDebug()) {
            System.out.print("Phase 2: Push cheapest to A: " + cheapestValue + " at index " + cheapestIndex+ " with cost " + cheapestCost+ " ("+tied+") with rotation "+rotation);
        }
        rotate(rotation);
        // Pousser dans A
        pa();
        if (isDebug()) {
            System.out.println(" => Stacks: " + stackA + " " + stackB);
        }
    }
    
    private void finalRotation(Rotation rotation) {
        // Trouver l'index du plus petit élément
        int minIndex = stackA.getHeadIndex();
        
        // Rotation pour mettre le plus petit en haut
        rotation.clear();
        if (minIndex <= stackA.size() / 2) {
            if (isDebug()) {
                System.out.println("Final rotation: " + minIndex + " forward rotations");
            }
            rotation.ra = minIndex;
        } else {
            if (isDebug()) {
                System.out.println("Final rotation: " + (stackA.size() - minIndex) + " reverse rotations");
            }
            rotation.rra = stackA.size() - minIndex;
        }
        rotate(rotation);
    }
    
    // ============ UTILITAIRES ============
    
    public void printResult() {
        System.out.println("Nombre d'opérations : " + operations.size());
        for (Operation op : operations) {
            System.out.print(op + " ");
        }
        System.out.println();
    }
    
    public void printStacks() {
        System.out.println("Stack A (top to bottom): " + stackA);
        System.out.println("Stack B (top to bottom): " + stackB);
    }
    
    // ============ MAIN POUR TESTER ============
    
    public static void main(String[] args) {
        /*
        // Test avec l'exemple problématique de l'utilisateur
        System.out.println("=== Test avec l'exemple : 3 4 5 2 7 9 0 6 1 8 ===");
        int[] testNumbers = {3, 4, 5, 2, 7, 9, 0, 6, 1, 8};
        Turk psTest = new Turk(testNumbers);
        psTest.setDebug(true);
        System.out.println("Avant tri:");
        psTest.printStacks();
        
        psTest.sort();
        
        System.out.println("\nAprès tri:");
        psTest.printStacks();
        System.out.println("Trié : " + psTest.isSorted());
        psTest.printResult();*/

        // Test avec 500 nombres aléatoires
        System.out.println("\n\n=== Test avec 500 nombres aléatoires ===");
        int[] numbers = new IntegerListGenerator().generate(500);
        
        Turk ps = new Turk(numbers);
        ps.setDebug(true);
        ps.sort();
        System.out.println("Nombre d'opérations : " + ps.getOperations().size());
        System.out.println("Trié : " + ps.isSorted());

//        ps.printResult();
    }
}
