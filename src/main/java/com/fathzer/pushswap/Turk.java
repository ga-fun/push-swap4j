package com.fathzer.pushswap;

import java.util.*;

//FIXME Probably do not work if size = 2
public class Turk extends PushSwapSorter {
    private boolean debug = false;
    
    public Turk(int[] numbers) {
        super(IntegerListGenerator.normalize(numbers));
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void sort() {
        if (isSorted()) return;
        
        // Phase 1 : Pousser tout dans B sauf 3 éléments
        while (stackA.size() > 3) {
            pb();
        }
        
        // Trier les 3 éléments restants dans A
        sortThree();
        
        if (debug) {
            System.out.println("Phase 1: 3 elements sorted in A, others in B");
            printStacks();
        }
        
        // Phase 2 : Ramener les éléments de B vers A de manière optimale
        while (!stackB.isEmpty()) {
            pushCheapestToA();
        }
        
        // Rotation finale pour avoir le plus petit en haut
        finalRotation();
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
        } else if (top > mid && mid < bot && top > bot) {
            // Already sorted but rotated
        } else if (top < mid && mid > bot && top < bot) {
            sa();
        } else if (top < mid && mid > bot && top > bot) {
            // Already sorted but rotated
        }
    }
    
    private void pushCheapestToA() {
        int cheapestIndex = -1;
        int cheapestCost = Integer.MAX_VALUE;
        
        // Parcourir tous les éléments de B
        for (int i = 0; i < stackB.size(); i++) {
            int value = stackB.get(i);
            int cost = calculateCost(value, i);
            
            if (cost < cheapestCost) {
                cheapestCost = cost;
                cheapestIndex = i;
            }
        }
        
        // Exécuter les mouvements pour l'élément le moins cher
        int value = stackB.get(cheapestIndex);
        if (debug) {
            System.out.print("Phase 2: Push cheapest to A: " + value + " at index " + cheapestIndex+ " with cost " + cheapestCost);
        }
        executeMove(value, cheapestIndex);
    }
    
    private int calculateCost(int value, int indexInB) {
        // Coût pour amener l'élément en haut de B
        int costB = Math.min(indexInB, stackB.size() - indexInB);
        
        // Trouver la position cible dans A
        int targetIndex = stackA.findTargetPosition(value);
        
        // Coût pour amener la position cible en haut de A
        int costA = Math.min(targetIndex, stackA.size() - targetIndex);
        
        // Optimisation : si les deux rotations vont dans le même sens
        boolean bothRotate = indexInB <= stackB.size() / 2 && targetIndex <= stackA.size() / 2;
        boolean bothReverseRotate = indexInB > stackB.size() / 2 && targetIndex > stackA.size() / 2;
        
        return (bothRotate || bothReverseRotate) ? Math.max(costA, costB) : costA + costB;
    }
    
    private void executeMove(int value, int indexInB) {
        int targetIndexA = stackA.findTargetPosition(value);

        if (debug) {
            System.out.println(" to A index: " + targetIndexA);
        }
        
        // Déterminer la direction des rotations
        boolean rotateB = indexInB <= stackB.size() / 2;
        boolean rotateA = targetIndexA <= stackA.size() / 2;
        
        int stepsB = rotateB ? indexInB : stackB.size() - indexInB;
        int stepsA = rotateA ? targetIndexA : stackA.size() - targetIndexA;
        
        // Rotations simultanées si même direction
        if (rotateB && rotateA) {
            int simultaneous = Math.min(stepsB, stepsA);
            for (int i = 0; i < simultaneous; i++) {
                rr();
            }
            stepsB -= simultaneous;
            stepsA -= simultaneous;
        } else if (!rotateB && !rotateA) {
            int simultaneous = Math.min(stepsB, stepsA);
            for (int i = 0; i < simultaneous; i++) {
                rrr();
            }
            stepsB -= simultaneous;
            stepsA -= simultaneous;
        }
        
        // Finir la rotation de B
        if (rotateB) {
            for (int i = 0; i < stepsB; i++) {
                rb();
            }
        } else {
            for (int i = 0; i < stepsB; i++) {
                rrb();
            }
        }
        
        // Finir la rotation de A
        if (rotateA) {
            for (int i = 0; i < stepsA; i++) {
                ra();
            }
        } else {
            for (int i = 0; i < stepsA; i++) {
                rra();
            }
        }
        
        // Pousser dans A
        pa();
        if (debug) {
            System.out.println("Stacks: " + stackA + " " + stackB);
        }
    }
    
    private void finalRotation() {
        // Trouver l'index du plus petit élément
        int minIndex = 0;
        int minValue = stackA.get(0);
        
        for (int i = 1; i < stackA.size(); i++) {
            if (stackA.get(i) < minValue) {
                minValue = stackA.get(i);
                minIndex = i;
            }
        }
        
        // Rotation pour mettre le plus petit en haut
        if (minIndex <= stackA.size() / 2) {
            if (debug) {
                System.out.println("Final rotation: " + minIndex + " forward rotations");
            }
            for (int i = 0; i < minIndex; i++) {
                ra();
            }
        } else {
            if (debug) {
                System.out.println("Final rotation: " + (stackA.size() - minIndex) + " reverse rotations");
            }
            int revSteps = stackA.size() - minIndex;
            for (int i = 0; i < revSteps; i++) {
                rra();
            }
        }
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
        ps.sort();
        System.out.println("Nombre d'opérations : " + ps.getOperations().size());
        System.out.println("Trié : " + ps.isSorted());

        ps.printResult();
    }
}
