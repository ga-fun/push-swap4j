package com.fathzer.pushswap.bfs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * Base class for breadth-first search algorithms.
 * @param <O> The type of operations to perform
 * @param <N> The type of nodes in the search space
 */
public class BFS<O, N extends Node<O>> {
    private BFSLogger<O, N> logger = new BFSLogger<>(){};

    public BFSLogger<O,N> getLogger() {
        return logger;
    }
    
    public void setLogger(BFSLogger<O,N> logger) {
        this.logger = logger;
    }

    public List<O> solve(N start, Predicate<N> isTarget) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(isTarget);
        logger.startSearch(start);

        Queue<N> queue = new LinkedList<>();
        Map<N, Integer> visited = new HashMap<>();
        
        queue.add(start);
        visited.put(start, 0);

        while (!queue.isEmpty()) {
            N curr = queue.poll();
            logger.enterNode(curr);

            if (isTarget.test(curr)) {
                logger.targetFound(curr);
                return curr.path();
            }

            Iterable<O> ops = curr.getOperations();
            for (O op : ops) {
                logger.applyOperation(curr, op);
                N next = curr.isRejected(op) ? null : curr.next(op);
                logger.nextStateBuilt(curr, op, next);
                if (next != null && (!visited.containsKey(next) || visited.get(next) > next.cost())) {
                    // Visit again the node only if its cost is lower than the previous one
                    next.validate(op);
                    visited.put(next, next.cost());
                    queue.add(next);
                    logger.nodeValidated(next, isTarget);
                }
            }
        }
        return List.of();
    }
}
