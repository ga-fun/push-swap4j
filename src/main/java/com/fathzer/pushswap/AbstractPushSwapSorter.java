package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractPushSwapSorter implements PushSwapManager {
    private boolean debug = false;

    protected Stack stackA;
    protected Stack stackB;
    protected List<Operation> operations;

    protected AbstractPushSwapSorter(int[] numbers) {
        stackA = new Stack(numbers);
        stackB = new Stack(numbers.length);
        operations = new LinkedList<>();
    }

    @Override
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
    
    @Override
    public void sb() {
        swap(stackB);
        operations.add(SB);
    }

    @Override
    public void ss() {
        swap(stackA);
        swap(stackB);
        operations.add(SS);
    }
    
    @Override
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
    
    @Override
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
    
    @Override
    public void ra() {
        stackA.rotateForward();
        operations.add(RA);
    }
    
    @Override
    public void rb() {
        stackB.rotateForward();
        operations.add(RB);
    }
    
    @Override
    public void rr() {
        stackA.rotateForward();
        stackB.rotateForward();
        operations.add(RR);
    }
    
    @Override
    public void rra() {
        stackA.rotateBackward();
        operations.add(RRA);
    }
    
    @Override
    public void rrb() {
        stackB.rotateBackward();
        operations.add(RRB);
    }
    
    @Override
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

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    protected boolean isDebug() {
        return debug;
    }

    public abstract void sort();
}
