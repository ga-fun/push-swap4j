package com.fathzer.pushswap.bfs;

import java.util.List;

public interface Node<T> {

    Iterable<T> getOperations();

    List<T> path();

    int cost();

    boolean isRejected(T operation);

    /**
     * Creates a new node by applying the given operation to this node.
     * @param op The operation to apply
     * @return A new node representing the state after applying the operation, or null if the operation is not applicable
     * If a new state is returned, it should have its cost updated. Its path could be updated later in the validate method.
     */
    <N extends Node<T>> N next(T op);
    
    /**
     * Validates this node.
     * <br>This method is called before a node is added to the search queue. The default implementation does nothing.
     * But you may override it to perform additional validation or update the node's state (for instance, add the operation to the path).
     * @param op The operation that was applied to reach this node
     */
    default void validate(T op) {
        // Default implementation does nothing
    }
}