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
    private BFSLogger<O, N> logger = new BFSLogger<>(){};

    public int nodeCount() {
        return nodeCount;
    }

    public void setLogger(BFSLogger logger) {
        this.logger = logger;
    }

    public List<O> solve(N start, Predicate<N> isTarget) {
        logger.startSearch(this, start);
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
                logger.targetFound(this, curr);
                return curr.path();
            }

            Iterable<O> ops = curr.getOperations();
            for (O op : ops) {
                logger.applyOperation(this, curr, op);
                N next = curr.isRejected(op) ? null : curr.next(op);
                logger.nextStateBuilt(this, curr, op, next);
                if (next != null && (!visited.containsKey(next) || visited.get(next) > next.cost())) {
                    next.validate(op);
                    visited.put(next, next.cost());
                    queue.add(next);
                    logger.nodeValidated(this, next, isTarget);
                }
            }
        }
        return List.of();
    }
}
