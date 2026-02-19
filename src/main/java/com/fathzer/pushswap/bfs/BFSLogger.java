package com.fathzer.pushswap.bfs;

import java.util.function.Predicate;

public interface BFSLogger<O, N extends Node<O>> {

    default void startSearch(BFS<O, N> bfs, N start) {}

    default void targetFound(BFS<O, N> bfs, N node) {}

    default void applyOperation(BFS<O,N> bfs, N curr, O op) {}

    default void nextStateBuilt(BFS<O,N> bfs, N curr, O op, N next) {}

    default void nodeValidated(BFS<O,N> bfs, N next, Predicate<N> isTarget) {}
}
