package com.fathzer.pushswap;

import java.util.List;

public class Checker extends PushSwapSorter {
    private List<Operation> checkedOperations;
    
    public Checker(int[] numbers, List<Operation> operations) {
        super(numbers);
        this.checkedOperations = operations;
    }

    @Override
    public void sort() {
        checkedOperations.forEach(this::makeMove);
    }
}
