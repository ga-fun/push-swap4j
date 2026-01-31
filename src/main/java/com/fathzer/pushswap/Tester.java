package com.fathzer.pushswap;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Tester implements AutoCloseable {
    public static void main(String[] args) {
        try (Tester tester = new Tester(8)) {
            doTest(tester, Turk::new, 100, 10000);
            doTest(tester, Turk::new, 500, 5000);
            System.out.println("Done");
        }
    }

    private static void doTest(Tester tester, Function<int[], PushSwapSorter> sorterBuilder, int size, int nbLoops) {
        int nbOperations = tester.testLoop(sorterBuilder, size, nbLoops);
        System.out.println("Average number of operations for " + size + " elements and " + nbLoops + " loops: " + nbOperations / nbLoops);
    }

    private final IntegerListGenerator generator;
    private ExecutorService executor;

    public Tester(int nbThreads) {
        this.generator = new IntegerListGenerator();
        this.executor = Executors.newFixedThreadPool(nbThreads);
    }

    public int testLoop(Function<int[], PushSwapSorter> sorterBuilder, int size, int nbLoops) {
        List<Future<Integer>> futures = IntStream.range(0, nbLoops).mapToObj(i -> executor.submit(() -> test(sorterBuilder, size).size())).toList();
        int totalOperations = 0;
        for (Future<Integer> future : futures) {
            try {
                totalOperations += future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
        return totalOperations;
    }

    public List<Operation> test(Function<int[], PushSwapSorter> sorterBuilder, int size) {
        int[] numbers = generator.generate(size);
        PushSwapSorter sorter = sorterBuilder.apply(numbers);
        sorter.sort();

        Checker checker = new Checker(numbers, sorter.getOperations());
        checker.sort();
        if (!checker.isSorted()) {
            throw new IllegalStateException("Checker failed on " + List.of(numbers));
        }
        return sorter.getOperations();
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
