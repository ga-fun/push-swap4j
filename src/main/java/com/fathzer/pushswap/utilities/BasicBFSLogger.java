package com.fathzer.pushswap.utilities;

import java.util.function.Predicate;

import com.fathzer.pushswap.bfs.BFSLogger;
import com.fathzer.pushswap.bfs.Node;

public class BasicBFSLogger<O, N extends Node<O>> implements BFSLogger<O, N> {
    private int nodeCount;
    private boolean debug;

    @Override
    public void startSearch(N start) {
        if (debug) {
            System.out.println("Searchingfrom state: " + start);
        }
    }

    @Override
    public void enterNode(N node) {
        nodeCount++;
        if (debug) {
            System.out.println("Entering " + node);
        }
    }

    @Override
    public void targetFound(N node) {
        if (debug) {
            System.out.println("Found target with path " + node.path() + " after exploring " + nodeCount + " nodes");
        }
    }

    @Override
    public void applyOperation(N curr, O op) {
        if (debug) {
            System.out.println("Applying operation: " + op + " to state: " + curr);
        }
    }

    @Override
    public void nextStateBuilt(N curr, O op, N next) {
        if (debug) {
            System.out.println("  > Next state: " + next);
        }
    }

    @Override
    public void nodeValidated(N next, Predicate<N> isTarget) {
        if (debug) {
            System.out.println("  > Adding " + next + " to queue");
        if (isTarget.test(next)) {
                System.out.println("  >>>>> Target found!!!!");
            }
        }
     }

    public int getVisitedNodeCount() {
        return nodeCount;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
