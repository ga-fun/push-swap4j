package com.fathzer.pushswap.utilities;

import java.util.function.Predicate;

import com.fathzer.pushswap.bfs.BFS;
import com.fathzer.pushswap.bfs.BFSLogger;
import com.fathzer.pushswap.bfs.Node;

public class BasicBFSLogger<O, N extends Node<O>> implements BFSLogger<O, N> {

    @Override
    public void startSearch(BFS<O, N> bfs, N start) {
        System.out.println("Searchingfrom state: " + start);
    }

    @Override
    public void targetFound(BFS<O, N> bfs, N node) {
        System.out.println("Found target with path " + node.path() + " after exploring " + bfs.nodeCount() + " nodes");
    }

    @Override
    public void applyOperation(BFS<O, N> bfs, N curr, O op) {
        System.out.println("Applying operation: " + op + " to state: " + curr);
    }

    @Override
    public void nextStateBuilt(BFS<O, N> bfs, N curr, O op, N next) {
        System.out.println("  > Next state: " + next);
    }

    @Override
    public void nodeValidated(BFS<O, N> bfs, N next, Predicate<N> isTarget) {
        System.out.println("  > Adding " + next + " to queue");
        if (isTarget.test(next)) {
            System.out.println("  >>>>> Target found!!!!");
        }
    }
}
