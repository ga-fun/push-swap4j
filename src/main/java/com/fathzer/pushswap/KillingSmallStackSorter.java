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
public class KillingSmallStackSorter {
    private interface Node extends PushSwapManager{
    }

    /**
     * Represents a state in the search space containing the current configuration
     * of both stacks, the path taken to reach this state, and associated costs.
     */
    private static class State implements Node {
        /** The values of stack A as long */
        long a;
        /** The length of stack A */
        int aLen;

        /** The values of stack B as long */
        long b;
        /** The length of stack B */
        int bLen;

        /** The sequence of operations taken to reach this state */
        List<Operation> path;
        /** The total cost (number of operations) to reach this state */
        int cost;
        /** The number of elements currently pushed to stack B */
        int pushedToB;

        State(int aLen, long aData, int bLen, long bData) {
            this.a = aData;
            this.aLen = aLen;
            this.b = bData;
            this.bLen = bLen;
            this.path = new ArrayList<>();
            this.cost = 0;
            this.pushedToB = 0;
        }

        /**
         * Creates a new state with the specified stack configurations.
         * 
         * @param a The initial state of stack A
         * @param b The initial state of stack B
         */
        State(List<Integer> a, List<Integer> b) {
            this(a.size(), FastStack.from(a.stream().mapToInt(Integer::intValue).toArray()),
                b.size(), FastStack.from(b.stream().mapToInt(Integer::intValue).toArray()));
            this.path = new ArrayList<>();
            this.cost = 0;
            this.pushedToB = 0;
        }

        @Override
        public void pb() {
            int value = FastStack.peek(a);
            this.b = FastStack.push(this.b, value);
            this.a = FastStack.deleteHead(this.a);
            this.aLen--;
            this.bLen++;
            this.pushedToB++;
        }

        @Override
        public void pa() {
            int value = FastStack.peek(b);
            this.a = FastStack.push(this.a, value);
            this.b = FastStack.deleteHead(this.b);
            this.aLen++;
            this.bLen--;
            this.pushedToB--;
        }

        @Override
        public void sa() {
            this.a = FastStack.swap(this.a);
        }

        @Override
        public void sb() {
            this.b = FastStack.swap(this.b);
        }

        @Override
        public void ss() {
            this.a = FastStack.swap(this.a);
            this.b = FastStack.swap(this.b);
        }

        @Override
        public void ra() {
            this.a = FastStack.rotateForward(this.a, this.aLen);
        }

        @Override
        public void rra() {
            this.a = FastStack.rotateBackward(this.a, this.aLen);
        }

        @Override
        public void rb() {
            this.b = FastStack.rotateForward(this.b, this.bLen);
        }

        @Override
        public void rrb() {
            this.b = FastStack.rotateBackward(this.b, this.bLen);
        }

        @Override
        public void rr() {
            this.a = FastStack.rotateForward(this.a, this.aLen);
            this.b = FastStack.rotateForward(this.b, this.bLen);
        }

        @Override
        public void rrr() {
            this.a = FastStack.rotateBackward(this.a, this.aLen);
            this.b = FastStack.rotateBackward(this.b, this.bLen);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = Long.hashCode(a);
            result = prime * result + aLen;
            result = prime * result + Long.hashCode(b);
            result = prime * result + bLen;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            State other = (State) obj;
            if (a != other.a || b != other.b) return false;
            return aLen == other.aLen && bLen == other.bLen;
        }

        @Override
        public String toString() {
            return FastStack.toString(a, aLen) + " | " + FastStack.toString(b, bLen) +": path=" + path + " cost=" + cost + " pushedToB=" + pushedToB;
        }
    }

    private int nodeCount = 0;
    private boolean debug = false;
    
    /**
     * Main method for testing the optimal sorter with all permutations of a target list.
     * Generates all permutations of the target list and finds optimal solutions for each.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        List<List<Integer>> allPerms = new ArrayList<>();
        List<Integer> target = Arrays.asList(0, 1, 2, 3, 4, 5);
        KillingSmallStackSorter sorter = new KillingSmallStackSorter();

        generatePerms(new ArrayList<>(target), 0, allPerms);
        System.out.println("Generated " + allPerms.size() + " permutations");
        int total = 0;
        long start = System.currentTimeMillis();
        
        for (List<Integer> startA : allPerms) {
            List<Operation> optimalPath = sorter.solve(startA, target);
//            System.out.println("case " + startA + ": "+optimalPath);
            total += optimalPath.size();

            Checker checker = new Checker(startA.stream().mapToInt(Integer::intValue).toArray(), optimalPath);
            checker.sort();
            if (!checker.isSorted()) {
                System.out.println("Error when searching for solution for: " + startA);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
        System.out.println("Total operations: " + total);
        System.out.println("Explored " + sorter.nodeCount + " nodes");
    }

    /**
     * Finds the optimal sequence of operations to transform stack A from start configuration
     * to target configuration using breadth-first search.
     * 
     * <p>This method avoids operations that would rotate
     * stack B or mix the bottom of stack B to maintain consistency.</p>
     * 
     * @param startA The initial configuration of stack A
     * @param targetAsList The target sorted configuration of stack A
     * @return A list of operations representing the optimal path, or empty list if no solution found
     */
    public List<Operation> solve(List<Integer> startA, List<Integer> targetAsList) {
        if (startA.size()!=targetAsList.size()) {
            throw new IllegalArgumentException("Start and target lists must have the same size");
        }
        if (startA.size()>=16) {
            throw new IllegalArgumentException("Start list must have less than 16 elements");
        }
        startA = Arrays.stream(IntegerListGenerator.normalize(startA.stream().mapToInt(Integer::intValue).toArray())).boxed().toList();
        targetAsList = Arrays.stream(IntegerListGenerator.normalize(targetAsList.stream().mapToInt(Integer::intValue).toArray())).boxed().toList();

        Queue<State> queue = new LinkedList<>();
        Map<State, Integer> visited = new HashMap<>();
        
        State root = new State(startA, new ArrayList<>());
        State target = new State(targetAsList, new ArrayList<>());
        queue.add(root);
        visited.put(root, 0);

        // On retire tout ce qui fait tourne B ou qui mélange le fond de B
        Operation[] ops = {SA, PA, PB, RA, RRA, SS};
        while (!queue.isEmpty()) {
            State curr = queue.poll();
            nodeCount++;

            if (curr.equals(target)) {
                if (debug) {
                    System.out.println("Explored " + nodeCount + " nodes");
                }
                return curr.path;
            }

            Operation lastOp = curr.path.isEmpty() ? null : curr.path.get(curr.path.size() - 1);

            for (Operation op : ops) {
                if (debug) {
                    System.out.println("Applying operation: " + op + " to state: " + curr);
                }
                State next = op.isNonOptimal(lastOp) ? null : apply(curr, op);
                if (debug) {
                    System.out.println("  > Next state: " + (next == null ? "null" : FastStack.toString(next.a, next.aLen) + " | " + FastStack.toString(next.b, next.bLen)));
                }
                if (next != null && (!visited.containsKey(next) || visited.get(next) > curr.cost + 1)) {
                    if (!curr.path.isEmpty()) {
                        next.path.addAll(curr.path);
                    }
                    next.path.add(op);
                    next.cost = curr.cost + 1;
                    next.pushedToB += curr.pushedToB;
                    visited.put(next, next.cost);
                    queue.add(next);
                    if (debug) {
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
        if (s.aLen==0 && PB.equals(op)) {
            return null;
        }
        if (s.aLen<2 && (SA.equals(op) || SS.equals(op) || RA.equals(op) || RRA.equals(op))) {
            return null;
        }
        State result = new State(s.aLen, s.a, s.bLen, s.b);
        op.apply(result);
        return result;
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
