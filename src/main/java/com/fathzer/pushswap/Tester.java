package com.fathzer.pushswap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.fathzer.pushswap.buterfly.Butterfly;
import com.fathzer.pushswap.buterfly.LisButterfly;

public class Tester implements AutoCloseable {
    public static void main(String[] args) {
        try (Tester tester = new Tester(8)) {
            Function<int[], AbstractPushSwapSorter> sorterBuilder = LisButterfly::new;
            tester.debugTest(sorterBuilder, 100);
//          System.exit(-1);

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

    private static void doTest(Tester tester, Function<int[], AbstractPushSwapSorter> sorterBuilder, int size, int nbLoops) {
        int nbOperations = tester.testLoop(sorterBuilder, size, nbLoops);
        System.out.println("Average number of operations for " + size + " elements and " + nbLoops + " loops: " + nbOperations / nbLoops);
    }

    private final IntegerListGenerator generator;
    private ExecutorService executor;

    public Tester(int nbThreads) {
        this.generator = new IntegerListGenerator();
        this.executor = Executors.newFixedThreadPool(nbThreads);
    }

    private void debugTest(Function<int[], AbstractPushSwapSorter> sorterBuilder, int size) {
        // int[] numbers = {9, 7, 6, 8, 2, 1, 3, 5, 0, 4};
//        //int[] numbers = {35, 2, 30, 0, 20, 25, 29, 18, 23, 26, 15, 48, 34, 21, 8, 17, 11, 16, 36, 3, 40, 6, 9, 49, 47, 38, 4, 27, 42, 37, 5, 44, 28, 13, 41, 43, 10, 32, 1, 14, 12, 31, 19, 24, 22, 39, 46, 7, 33, 45};
        //int[] numbers = {40, 69, 68, 25, 95, 76, 16, 15, 84, 87, 96, 27, 3, 49, 70, 65, 2, 90, 20, 6, 77, 81, 50, 72, 44, 14, 98, 59, 36, 7, 64, 53, 4, 73, 43, 18, 51, 46, 47, 5, 66, 48, 1, 23, 83, 45, 86, 35, 63, 54, 75, 94, 60, 32, 41, 88, 62, 71, 31, 13, 11, 0, 33, 82, 9, 10, 91, 56, 28, 42, 12, 17, 61, 39, 80, 92, 19, 29, 52, 74, 34, 78, 55, 85, 93, 24, 8, 21, 99, 37, 58, 89, 26, 67, 22, 38, 97, 79, 30, 57};
//        int[] numbers = IntegerListGenerator.normalize(generator.generate(size));
        int[] numbers = {77, 38, 76, 13, 75, 68, 26, 60, 34, 90, 66, 71, 20, 31, 49, 65, 79, 48, 96, 61, 53, 50, 70, 94, 88, 95, 36, 1, 8, 51, 92, 57, 4, 40, 55, 54, 14, 58, 23, 97, 69, 12, 27, 3, 24, 21, 85, 52, 19, 80, 89, 62, 30, 25, 86, 7, 42, 17, 6, 11, 47, 32, 22, 29, 64, 10, 67, 16, 74, 45, 93, 33, 43, 87, 35, 56, 98, 72, 37, 99, 83, 73, 63, 39, 15, 59, 28, 5, 44, 91, 46, 0, 78, 81, 9, 84, 2, 82, 18, 41};
        System.out.println("Basic test with " + numbers.length + " elements: " + Arrays.toString(numbers));
        AbstractPushSwapSorter sorter = sorterBuilder.apply(numbers);
        sorter.setDebug(true);
        sorter.sort();
        System.out.println("Result: "+sorter.getOperations().size()+" ("+sorter.getOperations()+")");
        Checker checker = new Checker(numbers, sorter.getOperations());
        checker.sort();
        if (!checker.isSorted()) {
            throw new IllegalStateException("Checker failed on " + Arrays.toString(numbers));
        }
    }

    public int testLoop(Function<int[], AbstractPushSwapSorter> sorterBuilder, int size, int nbLoops) {
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

    public List<Operation> test(Function<int[], AbstractPushSwapSorter> sorterBuilder, int size) {
        int[] numbers = generator.generate(size);
        AbstractPushSwapSorter sorter = sorterBuilder.apply(numbers);
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
