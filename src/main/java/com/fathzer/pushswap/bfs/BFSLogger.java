package com.fathzer.pushswap.bfs;

import java.util.function.Predicate;

/**
 * A logger interface for Breadth-First Search (BFS) operations.
 * 
 * <p>This interface provides callback methods that allow monitoring and logging
 * of various stages during BFS execution. It is designed to track the search
 * progress, node exploration, and operation application in a BFS algorithm.</p>
 * 
 * <p>Type parameters:</p>
 * <ul>
 *   <li>{@code O} - the type of operations that can be applied to nodes</li>
 *   <li>{@code N} - the type of nodes in the search space, must extend {@link Node}</li>
 * </ul>
 * 
 * @param <O> the type of operations that can be applied to nodes
 * @param <N> the type of nodes in the search space, extending Node<O>
 */
public interface BFSLogger<O, N extends Node<O>> {

    /**
     * Called when the BFS search starts.
     * 
     * <p>This method is invoked at the beginning of the BFS algorithm,
     * before any nodes are explored. It provides the BFS instance and the
     * starting node for the search.</p>
     * 
     * @param start the starting node of the search
     */
    default void startSearch(N start) {}

    /**
     * Called when entering a node during the search.
     * 
     * <p>This method is invoked when a node is about to be processed.</p>
     * 
     * @param node the node being entered
     */
    default void enterNode(N node) {}

    /**
     * Called when the target node is found during the search.
     * 
     * <p>This method is invoked when the BFS algorithm successfully finds
     * a node that satisfies the target condition. This typically indicates
     * the end of the search process.</p>
     * 
     * @param node the target node that was found
     */
    default void targetFound(N node) {}

    /**
     * Called when an operation is about to be applied to a node.
     * 
     * <p>This method is invoked just before an operation is applied to
     * generate a new state. This can be useful for tracking which operations
     * are being explored during the search.</p>
     * 
     * @param curr the current node to which the operation will be applied
     * @param op the operation that is about to be applied
     */
    default void applyOperation(N curr, O op) {}

    /**
     * Called after a new node is built by applying an operation.
     * 
     * <p>This method is invoked after an operation has been successfully
     * applied to a current node, resulting in a new node. The new node
     * has not yet been validated or added to the search queue.</p>
     * 
     * @param curr the current node from which the operation was applied
     * @param op the operation that was applied
     * @param next the newly built node resulting from the operation
     */
    default void nextStateBuilt(N curr, O op, N next) {}

    /**
     * Called when a node is validated against the target condition.
     * 
     * <p>This method is invoked after a node is built and validated to check
     * if it meets the target criteria. The validation predicate is provided
     * to allow custom target checking logic.</p>
     * 
     * @param next the node that is being validated
     * @param isTarget a predicate that determines if the node is the target
     */
    default void nodeValidated(N next, Predicate<N> isTarget) {}
}
