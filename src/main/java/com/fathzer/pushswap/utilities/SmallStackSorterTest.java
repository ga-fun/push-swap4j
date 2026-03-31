package com.fathzer.pushswap.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.fathzer.pushswap.Checker;
import com.fathzer.pushswap.SmallStackSorter;
import com.fathzer.pushswap.Operation;


public class SmallStackSorterTest {

    private static class FuckingBugLogger extends BasicBFSLogger<Operation, SmallStackSorter.State> {
        @Override
        public void applyOperation(SmallStackSorter.State curr, Operation op) {
            if (isSearchedPath(curr.path()) && SEARCHED_PATHS.get(curr.path().size()) == op) {
                super.applyOperation(curr, op);
            }
        }

        @Override
        public void nextStateBuilt(SmallStackSorter.State curr, Operation op, SmallStackSorter.State next) {
            if (isSearchedPath(curr.path()) && SEARCHED_PATHS.get(curr.path().size()) == op) {
                super.nextStateBuilt(curr, op, next);
            }
        }

        @Override
        public void nodeValidated(SmallStackSorter.State next, Predicate<SmallStackSorter.State> isTarget) {
            if (isSearchedPath(next.path())) {
                super.nodeValidated(next, isTarget);
            }
        }

        private static final List<Operation> SEARCHED_PATHS = List.of(Operation.PB, Operation.SA, Operation.RA, Operation.PA);
        private boolean isSearchedPath(List<Operation> path) {
            return SEARCHED_PATHS.stream().limit(path.size()).toList().equals(path);
        }
    }

    /**
     * Main method for testing the optimal sorter with all permutations of a target list.
     * Generates all permutations of the target list and finds optimal solutions for each.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SmallStackSorter sorter = new SmallStackSorter();
        sorter.setLogger(new BasicBFSLogger<>());
/* 
        List<Integer> initial = List.of(0, 2, 1, 3);
        List<Integer> goal = IntStream.range(0, initial.size()).boxed().toList();
        sorter.setLogger(new BasicBFSLogger<>());
        sorter.solve(initial, goal);
        sorter.setLogger(new BFSLogger<Operation, CleanKillingSmallStackSorter.State>() {});
        System.exit(0);*/

        // TODO: make this configurable
        int count = 7;
        List<int[]> allPerms = new ArrayList<>();
        int[] target = IntStream.range(0, count).toArray();
//        sorter.setDebug(true);

        generatePerms(target, 0, allPerms);
        System.out.println("Generated " + allPerms.size() + " permutations");
        int total = 0;
        List<Operation> longest = List.of();
        int[] longestCase = null;
        long start = System.currentTimeMillis();
        
        for (int[] startA : allPerms) {
            List<Operation> optimalPath = sorter.solve(startA, target);
//            System.out.println("case " + Arrays.toString(startA) + ": "+optimalPath);
            total += optimalPath.size();
            if (optimalPath.size() > longest.size()) {
                longest = optimalPath;
                longestCase = startA;
            }

            Checker checker = new Checker(startA, optimalPath);
            checker.sort();
            if (!checker.isSorted()) {
                System.out.println("Error when searching for solution for: " + Arrays.toString(startA) + ": " + optimalPath+" -> "+checker.getAStack());
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
        System.out.println("Total operations: " + total);
        System.out.println("Longest case (size=" + longest.size() + "): "+ longest + " for " + Arrays.toString(longestCase));
        System.out.println("Explored " + ((BasicBFSLogger<?, ?>)sorter.getLogger()).getVisitedNodeCount() + " nodes");
    }

    /**
     * Generates all permutations of a list using recursive backtracking.
     * 
     * @param l The list to permute (will be modified during recursion)
     * @param k The current position in the list
     * @param res The list to store all generated permutations
     */
    private static void generatePerms(int[] l, int k, List<int[]> res) {
        for(int i = k; i < l.length; i++){
            int temp = l[i];
            l[i] = l[k];
            l[k] = temp;
            generatePerms(l, k + 1, res);
            l[k] = l[i];
            l[i] = temp;
        }
        if (k == l.length -1) res.add(l.clone());
    }
}
