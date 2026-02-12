package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.*;

/**
 * An optimal stack sorter that finds the shortest sequence of operations to sort small stacks.
 * This class implements a breadth-first search algorithm to find the optimal solution
 * for sorting stacks using push-swap operations.
 * 
 * <p>The algorithm works with two stacks (A and B) and supports operations like
 * swap (SA, SB, SS), push (PA, PB), and rotate (RA, RRA). It uses BFS to explore
 * all possible states and find the shortest path from the initial configuration
 * to the target configuration.</p>
 * 
 */
public class OptimalSmallStackSorter {

    /**
     * Represents a state in the search space containing the current configuration
     * of both stacks, the path taken to reach this state, and associated costs.
     */
    private static class State {
        /** The current state of stack A */
        List<Integer> a;
        /** The current state of stack B */
        List<Integer> b;
        /** The sequence of operations taken to reach this state */
        List<Operation> path;
        /** The total cost (number of operations) to reach this state */
        int cost;
        /** The number of elements currently pushed to stack B */
        int pushedToB;

        /**
         * Creates a new state with the specified stack configurations.
         * 
         * @param a The initial state of stack A
         * @param b The initial state of stack B
         */
        State(List<Integer> a, List<Integer> b) {
            this.a = new ArrayList<>(a);
            this.b = new ArrayList<>(b);
            this.path = new ArrayList<>();
            this.cost = 0;
            this.pushedToB = 0;
        }

        /**
         * Generates a unique identifier for this state based on stack contents.
         * 
         * @return A string representation that uniquely identifies this state
         */
        String getId() { return a.toString() + "|" + b.toString(); }

        @Override
        public String toString() {
            return getId() + ": path=" + path + " cost=" + cost + " pushedToB=" + pushedToB;
        }
    }

    /**
     * Main method for testing the optimal sorter with all permutations of a target list.
     * Generates all permutations of the target list and finds optimal solutions for each.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        List<List<Integer>> allPerms = new ArrayList<>();
        List<Integer> target = Arrays.asList(0, 1, 2, 3, 4);
        generatePerms(new ArrayList<>(target), 0, allPerms);
        System.out.println("Generated " + allPerms.size() + " permutations");
        int total = 0;
        long start = System.currentTimeMillis();
        
        for (List<Integer> startA : allPerms) {
            List<Operation> optimalPath = solve(startA, target);
            System.out.println("case " + startA + ": "+optimalPath);
            total += optimalPath.size();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
        System.out.println("Total operations: " + total);
    }

    private static boolean DEBUG = false;

    /**
     * Finds the optimal sequence of operations to transform stack A from start configuration
     * to target configuration using breadth-first search.
     * 
     * <p>This method avoids operations that would rotate
     * stack B or mix the bottom of stack B to maintain consistency.</p>
     * 
     * @param startA The initial configuration of stack A
     * @param target The target sorted configuration of stack A
     * @return A list of operations representing the optimal path, or empty list if no solution found
     */
    public static List<Operation> solve(List<Integer> startA, List<Integer> target) {
        if (startA.size()!=target.size()) {
            throw new IllegalArgumentException("Start and target lists must have the same size");
        }
        if (startA.size()>=16) {
            throw new IllegalArgumentException("Start list must have less than 16 elements");
        }
        startA = Arrays.stream(IntegerListGenerator.normalize(startA.stream().mapToInt(Integer::intValue).toArray())).boxed().toList();
        target = Arrays.stream(IntegerListGenerator.normalize(target.stream().mapToInt(Integer::intValue).toArray())).boxed().toList();

        Queue<State> queue = new LinkedList<>();
        Map<String, Integer> visited = new HashMap<>();
        
        State root = new State(startA, new ArrayList<>());
        queue.add(root);
        visited.put(root.getId(), 0);

        int nodeCount = 0;

        // On retire tout ce qui fait tourne B ou qui mélange le fond de B
        Operation[] ops = {SA, PA, PB, RA, RRA, SS};
        while (!queue.isEmpty()) {
            State curr = queue.poll();
            nodeCount++;

            if (curr.a.equals(target)) {
                if (DEBUG) {
                    System.out.println("Explored " + nodeCount + " nodes");
                }
                return curr.path;
            }

            for (Operation op : ops) {
                if (DEBUG) {
                    System.out.println("Applying operation: " + op + " to state: " + curr);
                }
                State next = apply(curr, op);
                if (DEBUG && next != null) {
                    System.out.println("  > Next state: " + next.a+" | "+next.b);
                }
                if (next != null && (!visited.containsKey(next.getId()) || visited.get(next.getId()) > curr.cost + 1)) {
                    if (!curr.path.isEmpty()) {
                        next.path.addAll(curr.path);
                    }
                    next.path.add(op);
                    next.cost = curr.cost + 1;
                    if (PB.equals(op)) {
                        next.pushedToB = curr.pushedToB + 1;
                    } else if (PA.equals(op)) {
                        next.pushedToB = curr.pushedToB - 1;
                    } else {
                        next.pushedToB = curr.pushedToB;
                    }
                    visited.put(next.getId(), next.cost);
                    queue.add(next);
                    if (DEBUG) {
                        System.out.println("  > Adding " + next + " to queue");
                    }
                }
            }
        }
        return List.of();
    }

    /**
     * Applies an operation to a state and returns the resulting state.
     * Performs validation to ensure the operation is valid for the current state.
     * 
     * @param s The current state
     * @param op The operation to apply
     * @return The new state after applying the operation, or null if the operation is invalid
     */
    private static State apply(State s, Operation op) {
        if (s.pushedToB == 0 && PA.equals(op)) {
            return null;
        }
        if (s.pushedToB < 2 && (SS.equals(op) || SB.equals(op))) {
            return null;
        }
        if (s.a.isEmpty() && PB.equals(op)) {
            return null;
        }
        if (s.a.size()<2 && (SA.equals(op) || SS.equals(op) || RA.equals(op) || RRA.equals(op))) {
            return null;
        }
        List<Integer> na = SB.equals(op) ? s.a : new ArrayList<>(s.a);
        List<Integer> nb = SA.equals(op) || RA.equals(op) || RRA.equals(op) ? s.b : new ArrayList<>(s.b);
        switch (op) {
            case SA: Collections.swap(na, 0, 1); break;
            case SB: Collections.swap(nb, 0, 1); break;
            case SS: Collections.swap(na, 0, 1); Collections.swap(nb, 0, 1); break;
            case PB: nb.add(0, na.remove(0)); break;
            case PA: na.add(0, nb.remove(0)); break;
            case RA: na.add(na.remove(0)); break;
            case RRA: na.add(0, na.remove(na.size()-1)); break;
            default: return null;
        }
        return new State(na, nb);
    }

    /**
     * Generates all permutations of a list using recursive backtracking.
     * 
     * @param l The list to permute (will be modified during recursion)
     * @param k The current position in the list
     * @param res The list to store all generated permutations
     */
    private static void generatePerms(List<Integer> l, int k, List<List<Integer>> res) {
        for(int i = k; i < l.size(); i++){
            Collections.swap(l, i, k);
            generatePerms(l, k + 1, res);
            Collections.swap(l, k, i);
        }
        if (k == l.size() -1) res.add(new ArrayList<>(l));
    }
}
