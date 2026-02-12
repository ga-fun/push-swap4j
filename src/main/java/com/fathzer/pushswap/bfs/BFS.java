package com.fathzer.pushswap.bfs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;

public class BFS<O, N extends Node<O>> {
    private int nodeCount = 0;
    private boolean debug = false;

    public int nodeCount() {
        return nodeCount;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List<O> solve(N start, Predicate<N> isTarget) {
        Objects.requireNonNull(isTarget);

        Queue<N> queue = new LinkedList<>();
        Map<N, Integer> visited = new HashMap<>();
        
        queue.add(start);
        visited.put(start, 0);

        // On retire tout ce qui fait tourne B ou qui mélange le fond de B
        while (!queue.isEmpty()) {
            N curr = queue.poll();
            nodeCount++;

            if (isTarget.test(curr)) {
                // if (debug) {
                //     System.out.println("Explored " + nodeCount + " nodes");
                // }
                return curr.path();
            }

            Iterable<O> ops = curr.getOperations();

            for (O op : ops) {
                // if (debug) {
                //     System.out.println("Applying operation: " + op + " to state: " + curr);
                // }
                N next = curr.isRejected(op) ? null : curr.next(op);
                // if (debug) {
                //     System.out.println("  > Next state: " + (next == null ? "null" : FastStack.toString(next.a, next.aLen) + " | " + FastStack.toString(next.b, next.bLen)));
                // }
                if (next != null && (!visited.containsKey(next) || visited.get(next) > curr.cost())) {
                    visited.put(next, next.cost());
                    queue.add(next);
                    // if (debug) {
                    //     System.out.println("  > Adding " + next + " to queue");
                    // }
                }
            }
        }
        return List.of();
    }
}
