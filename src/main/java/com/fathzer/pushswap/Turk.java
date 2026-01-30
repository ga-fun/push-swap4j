package com.fathzer.pushswap;

import java.util.*;

public class Turk {
    private List<Integer> stackA;  // Index 0 = haut de la pile
    private List<Integer> stackB;  // Index 0 = haut de la pile
    private List<String> operations;
    
    public Turk(int[] numbers) {
        stackA = new ArrayList<>();
        stackB = new ArrayList<>();
        operations = new ArrayList<>();
        
        // Remplir la pile A - l'ordre du tableau correspond à l'ordre dans la pile
        for (int num : numbers) {
            stackA.add(num);
        }
    }
    
    // ============ OPÉRATIONS DE BASE ============
    
    private void sa() {
        if (stackA.size() < 2) return;
        int first = stackA.remove(0);
        int second = stackA.remove(0);
        stackA.add(0, first);
        stackA.add(0, second);
        operations.add("sa");
    }
    
    private void sb() {
        if (stackB.size() < 2) return;
        int first = stackB.remove(0);
        int second = stackB.remove(0);
        stackB.add(0, first);
        stackB.add(0, second);
        operations.add("sb");
    }
    
    private void pa() {
        if (stackB.isEmpty()) return;
        stackA.add(0, stackB.remove(0));
        operations.add("pa");
    }
    
    private void pb() {
        if (stackA.isEmpty()) return;
        stackB.add(0, stackA.remove(0));
        operations.add("pb");
    }
    
    private void ra() {
        if (stackA.size() < 2) return;
        int top = stackA.remove(0);
        stackA.add(top);
        operations.add("ra");
    }
    
    private void rb() {
        if (stackB.size() < 2) return;
        int top = stackB.remove(0);
        stackB.add(top);
        operations.add("rb");
    }
    
    private void rr() {
        boolean didRa = stackA.size() >= 2;
        boolean didRb = stackB.size() >= 2;
        if (didRa) {
            int top = stackA.remove(0);
            stackA.add(top);
        }
        if (didRb) {
            int top = stackB.remove(0);
            stackB.add(top);
        }
        if (didRa || didRb) operations.add("rr");
    }
    
    private void rra() {
        if (stackA.size() < 2) return;
        int bottom = stackA.remove(stackA.size() - 1);
        stackA.add(0, bottom);
        operations.add("rra");
    }
    
    private void rrb() {
        if (stackB.size() < 2) return;
        int bottom = stackB.remove(stackB.size() - 1);
        stackB.add(0, bottom);
        operations.add("rrb");
    }
    
    private void rrr() {
        boolean didRra = stackA.size() >= 2;
        boolean didRrb = stackB.size() >= 2;
        if (didRra) {
            int bottom = stackA.remove(stackA.size() - 1);
            stackA.add(0, bottom);
        }
        if (didRrb) {
            int bottom = stackB.remove(stackB.size() - 1);
            stackB.add(0, bottom);
        }
        if (didRra || didRrb) operations.add("rrr");
    }
    
    // ============ ALGORITHME TURK ============
    
    public void turkSort() {
        if (isSorted()) return;
        
        // Phase 1 : Pousser tout dans B sauf 3 éléments
        while (stackA.size() > 3) {
            pb();
        }
        
        // Trier les 3 éléments restants dans A
        sortThree();
        
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
        executeMove(value, cheapestIndex);
    }
    
    private int calculateCost(int value, int indexInB) {
        // Coût pour amener l'élément en haut de B
        int costB = Math.min(indexInB, stackB.size() - indexInB);
        
        // Trouver la position cible dans A
        int targetIndex = findTargetPositionInA(value);
        
        // Coût pour amener la position cible en haut de A
        int costA = Math.min(targetIndex, stackA.size() - targetIndex);
        
        // Optimisation : si les deux rotations vont dans le même sens
        boolean bothRotate = indexInB <= stackB.size() / 2 && targetIndex <= stackA.size() / 2;
        boolean bothReverseRotate = indexInB > stackB.size() / 2 && targetIndex > stackA.size() / 2;
        
        if (bothRotate || bothReverseRotate) {
            return Math.max(costA, costB);
        }
        
        return costA + costB;
    }
    
    private int findTargetPositionInA(int value) {
        // Trouver où insérer 'value' dans A pour garder A trié (ordre croissant)
        // On cherche le plus petit élément de A qui est plus grand que value
        
        int targetIndex = -1;
        int closestBigger = Integer.MAX_VALUE;
        
        for (int i = 0; i < stackA.size(); i++) {
            int elem = stackA.get(i);
            if (elem > value && elem < closestBigger) {
                closestBigger = elem;
                targetIndex = i;
            }
        }
        
        // Si pas trouvé (value est plus grand que tout dans A),
        // on doit le placer avant le plus petit élément de A
        if (targetIndex == -1) {
            int minIndex = 0;
            int minValue = stackA.get(0);
            for (int i = 1; i < stackA.size(); i++) {
                if (stackA.get(i) < minValue) {
                    minValue = stackA.get(i);
                    minIndex = i;
                }
            }
            targetIndex = minIndex;
        }
        
        return targetIndex;
    }
    
    private void executeMove(int value, int indexInB) {
        int targetIndexA = findTargetPositionInA(value);
        
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
            for (int i = 0; i < minIndex; i++) {
                ra();
            }
        } else {
            int revSteps = stackA.size() - minIndex;
            for (int i = 0; i < revSteps; i++) {
                rra();
            }
        }
    }
    
    // ============ UTILITAIRES ============
    
    private boolean isSorted() {
        if (!stackB.isEmpty()) return false;
        for (int i = 0; i < stackA.size() - 1; i++) {
            if (stackA.get(i) > stackA.get(i + 1)) {
                return false;
            }
        }
        return true;
    }
    
    public List<String> getOperations() {
        return operations;
    }
    
    public void printResult() {
        System.out.println("Nombre d'opérations : " + operations.size());
        for (String op : operations) {
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
        // Test avec l'exemple problématique de l'utilisateur
        System.out.println("=== Test avec l'exemple : 3 4 5 2 7 9 10 6 1 8 ===");
        int[] testNumbers = {3, 4, 5, 2, 7, 9, 10, 6, 1, 8};
        Turk psTest = new Turk(testNumbers);
        System.out.println("Avant tri:");
        psTest.printStacks();
        
        psTest.turkSort();
        
        System.out.println("\nAprès tri:");
        psTest.printStacks();
        System.out.println("Trié : " + psTest.isSorted());
        psTest.printResult();
        
        // Test avec 500 nombres aléatoires
        System.out.println("\n\n=== Test avec 500 nombres aléatoires ===");
        int[] numbers = new int[500];
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < 500; i++) {
            numbers[i] = list.get(i);
        }
        
        Turk ps = new Turk(numbers);
        ps.turkSort();
        System.out.println("Nombre d'opérations : " + ps.getOperations().size());
        System.out.println("Trié : " + ps.isSorted());
    }
    
    // ============ MAIN POUR TESTER ============
    
    public static void mainx(String[] args) {
        // Générer les nombres aléatoires
        int[] numbers = new int[500];
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= numbers.length; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = list.get(i);
        }
        
        Turk ps = new Turk(numbers);
        System.out.println("Pile A : " + ps.stackA);
        ps.turkSort();
        ps.printResult();
        
        // Vérifier que c'est trié
        System.out.println("\nTrié : " + ps.isSorted());
        if (!ps.isSorted()) {
            System.out.println("Erreur : la pile n'est pas triée !");
            System.out.println("Pile A : " + ps.stackA);
            System.out.println("Pile B : " + ps.stackB);
        } else {
            System.out.println("Succès : la pile est triée en " + ps.getOperations().size() + " opérations !");
        }
    }
}
