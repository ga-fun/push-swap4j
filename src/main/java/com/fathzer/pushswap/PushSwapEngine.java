package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.ArrayList;
import java.util.List;

public class PushSwapEngine {
    protected Stack stackA;
    protected Stack stackB;
    protected List<Operation> operations;

    public PushSwapEngine(int[] numbers) {
        stackA = new Stack(numbers);
        stackB = new Stack();
        operations = new ArrayList<>();
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
    
    public void pa() {
        if (stackB.isEmpty()) return;
        stackA.push(stackB.pop());
        operations.add(PA);
    }
    
    public void pb() {
        if (stackA.isEmpty()) return;
        stackB.push(stackA.pop());
        operations.add(PB);
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

    public List<Operation> getOperations() {
        return operations;
    }

    public boolean isSorted() {
        if (!stackB.isEmpty()) return false;
        for (int i = 0; i < stackA.size() - 1; i++) {
            if (stackA.get(i) > stackA.get(i + 1)) {
                return false;
            }
        }
        return true;
    }
}
