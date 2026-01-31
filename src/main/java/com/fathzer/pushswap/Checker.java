package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.List;

public class Checker extends PushSwapSorter {
    private List<Operation> checkedOperations;
    
    public Checker(int[] numbers, List<Operation> operations) {
        super(numbers);
        this.checkedOperations = operations;
    }

    @Override
    public void sort() {
        checkedOperations.forEach(this::apply);
    }
    
    private void apply(Operation op) {
        if (op == PA) {
            this.pa();
        } else if (op == PB) {
            this.pb();
        } else if (op == SA) {
            this.sa();
        } else if (op == SB) {
            this.sb();
        } else if (op == RA) {
            this.ra();
        } else if (op == RB) {
            this.rb();
        } else if (op == RR) {
            this.rr();
        } else if (op == RRA) {
            this.rra();
        } else if (op == RRB) {
            this.rrb();
        } else if (op == RRR) {
            this.rrr();
        }
    }
}
