package com.fathzer.pushswap;

import java.util.Arrays;
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
            Function<int[], PushSwapSorter> sorterBuilder = Butterfly::new;
            tester.debugTest(sorterBuilder, 50);

            doTest(tester, sorterBuilder, 100, 10000);
            // doTest(tester, sorterBuilder, 200, 5000);
            // doTest(tester, sorterBuilder, 300, 5000);
            // doTest(tester, sorterBuilder, 400, 5000);
            long startTime = System.currentTimeMillis();
            doTest(tester, sorterBuilder, 500, 5000);
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken: " + (endTime - startTime) + " ms");
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

    private void debugTest(Function<int[], PushSwapSorter> sorterBuilder, int size) {
        // int[] numbers = new int[]{9, 7, 6, 8, 2, 1, 3, 5, 0, 4};
        // int[] numbers = IntegerListGenerator.normalize(generator.generate(size));
        int[] numbers = new int[]{35, 2, 30, 0, 20, 25, 29, 18, 23, 26, 15, 48, 34, 21, 8, 17, 11, 16, 36, 3, 40, 6, 9, 49, 47, 38, 4, 27, 42, 37, 5, 44, 28, 13, 41, 43, 10, 32, 1, 14, 12, 31, 19, 24, 22, 39, 46, 7, 33, 45};
        System.out.println("Basic test with " + size + " elements: " + Arrays.toString(numbers));
        PushSwapSorter sorter = sorterBuilder.apply(numbers);
        sorter.setDebug(true);
        sorter.sort();
        System.out.println("Result: "+sorter.getOperations().size()+" ("+sorter.getOperations()+")");
        Checker checker = new Checker(numbers, sorter.getOperations());
        checker.sort();
        if (!checker.isSorted()) {
            throw new IllegalStateException("Checker failed on " + Arrays.toString(numbers));
        }
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
            throw new IllegalStateException("Checker failed on " + Arrays.toString(numbers));
        }
        return sorter.getOperations();
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
