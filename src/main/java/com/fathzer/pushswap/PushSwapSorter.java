package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.LinkedList;
import java.util.List;

public abstract class PushSwapSorter {
    protected Stack stackA;
    protected Stack stackB;
    protected List<Operation> operations;

    protected PushSwapSorter(int[] numbers) {
        stackA = new Stack(numbers);
        stackB = new Stack(numbers.length);
        operations = new LinkedList<>();
    }

    public void sa() {
        swap(stackA);
        operations.add(SA);
    }

    private boolean swap(Stack stack) {
        if (stack.size() < 2) return false;
        int first = stack.pop();
        int second = stack.pop();
        stack.push(first);
        stack.push(second);
        return true;
    }
    
    public void sb() {
        swap(stackB);
        operations.add(SB);
    }

    public void ss() {
        swap(stackA);
        swap(stackB);
        operations.add(SS);
    }
    
    public void pa() {
        operations.add(PA);
        if (!stackB.isEmpty()) {
            stackA.push(stackB.pop());
        }
    }

    public void pa(int count) {
        for (int i = 0; i < count; i++) {
            operations.add(PA);
        }
        if (!stackB.isEmpty()) {
            stackA.push(stackB, count);
        }
    }
    
    public void pb() {
        operations.add(PB);
        if (!stackA.isEmpty()) {
            stackB.push(stackA.pop());
        }
    }

    public void pb(int count) {
        for (int i = 0; i < count; i++) {
            operations.add(PB);
        }
        if (!stackA.isEmpty()) {
            stackB.push(stackA, count);
        }
    }
    
    public void ra() {
        stackA.rotateForward();
        operations.add(RA);
    }
    
    public void rb() {
        stackB.rotateForward();
        operations.add(RB);
    }
    
    public void rr() {
        stackA.rotateForward();
        stackB.rotateForward();
        operations.add(RR);
    }
    
    public void rra() {
        stackA.rotateBackward();
        operations.add(RRA);
    }
    
    public void rrb() {
        stackB.rotateBackward();
        operations.add(RRB);
    }
    
    public void rrr() {
        stackA.rotateBackward();
        stackB.rotateBackward();
        operations.add(RRR);
    }

    public void rotate(Rotation rotation) {
        for (int i = 0; i < rotation.ra; i++) {
            operations.add(RA);
        }
        for (int i = 0; i < rotation.rra; i++) {
            operations.add(RRA);
        }
        for (int i = 0; i < rotation.rb; i++) {
            operations.add(RB);
        }
        for (int i = 0; i < rotation.rrb; i++) {
            operations.add(RRB);
        }
        for (int i = 0; i < rotation.rr; i++) {
            operations.add(RR);
        }
        for (int i = 0; i < rotation.rrr; i++) {
            operations.add(RRR);
        }
        int raCount = rotation.ra + rotation.rr - rotation.rra - rotation.rrr;
        int rbCount = rotation.rb + rotation.rr - rotation.rrb - rotation.rrr;
        if (raCount < 0) {
            stackA.rotateBackward(-raCount);
        } else if (raCount > 0) {
            stackA.rotateForward(raCount);
        }
        if (rbCount < 0) {
            stackB.rotateBackward(-rbCount);
        } else if (rbCount > 0) {
            stackB.rotateForward(rbCount);
        }
    }

    public void makeMove(Operation op) {
        if (op == PA) {
            this.pa();
        } else if (op == PB) {
            this.pb();
        } else if (op == SA) {
            this.sa();
        } else if (op == SB) {
            this.sb();
        } else if (op == SS) {
            this.ss();
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

    public Stack getAStack() {
        return stackA;
    }

    public Stack getBStack() {
        return stackB;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public boolean isSorted() {
        return stackA.isSorted() && stackB.isEmpty();
    }

    public abstract void sort();
}
