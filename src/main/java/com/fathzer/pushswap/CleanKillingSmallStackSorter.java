package com.fathzer.pushswap;

import static com.fathzer.pushswap.Operation.*;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.pushswap.bfs.BFS;
import com.fathzer.pushswap.bfs.Node;

public class CleanKillingSmallStackSorter extends BFS<Operation, CleanKillingSmallStackSorter.State> {

    /**
     * Represents a state in the search space containing the current configuration
     * of both stacks, the path taken to reach this state, and associated costs.
     */
    public static class State implements Node<Operation>, PushSwapManager {
        private static final List<Operation> OPERATIONS = List.of(SA, SS, PA, PB, RA, RRA);

        /** The values of stack A as long */
        long a;
        /** The length of stack A */
        int aLen;

        /** The values of stack B as long */
        long b;
        /** The length of stack B */
        int bLen;

        /** The sequence of operations taken to reach this state */
        //TODO Maybe we could have a more compact representation of the path here
        List<Operation> path;
        /** The total cost (number of operations) to reach this state */
        int cost;
        /** The number of elements currently pushed to stack B */
        int pushedToB;

        private Operation last = null;

        State(State other) {
            this.a = other.a;
            this.aLen = other.aLen;
            this.b = other.b;
            this.bLen = other.bLen;
            this.path = new ArrayList<>();
            this.cost = 0;
            this.pushedToB = other.pushedToB;
        }

        /**
         * Creates a new state with the specified stack configurations.
         * 
         * @param a The initial state of stack A
         * @param b The initial state of stack B
         */
        State(int[] a, int[] b) {
            this.aLen = a.length;
            this.a = FastStack.from(a);
            this.bLen = b.length;
            this.b = FastStack.from(b);
            this.path = new ArrayList<>();
            this.cost = 0;
            this.pushedToB = 0;
        }

        @Override
        public void pb() {
            int value = FastStack.peek(a);
            this.b = FastStack.push(this.b, value);
            this.a = FastStack.deleteHead(this.a);
            this.aLen--;
            this.bLen++;
            this.pushedToB++;
        }

        @Override
        public void pa() {
            int value = FastStack.peek(b);
            this.a = FastStack.push(this.a, value);
            this.b = FastStack.deleteHead(this.b);
            this.aLen++;
            this.bLen--;
            this.pushedToB--;
        }

        @Override
        public void sa() {
            this.a = FastStack.swap(this.a);
        }

        @Override
        public void sb() {
            this.b = FastStack.swap(this.b);
        }

        @Override
        public void ss() {
            this.a = FastStack.swap(this.a);
            this.b = FastStack.swap(this.b);
        }

        @Override
        public void ra() {
            this.a = FastStack.rotateForward(this.a, this.aLen);
        }

        @Override
        public void rra() {
            this.a = FastStack.rotateBackward(this.a, this.aLen);
        }

        @Override
        public void rb() {
            this.b = FastStack.rotateForward(this.b, this.bLen);
        }

        @Override
        public void rrb() {
            this.b = FastStack.rotateBackward(this.b, this.bLen);
        }

        @Override
        public void rr() {
            this.a = FastStack.rotateForward(this.a, this.aLen);
            this.b = FastStack.rotateForward(this.b, this.bLen);
        }

        @Override
        public void rrr() {
            this.a = FastStack.rotateBackward(this.a, this.aLen);
            this.b = FastStack.rotateBackward(this.b, this.bLen);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = Long.hashCode(a);
            result = prime * result + aLen;
            result = prime * result + Long.hashCode(b);
            result = prime * result + bLen;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            State other = (State) obj;
            if (a != other.a || b != other.b) return false;
            return aLen == other.aLen && bLen == other.bLen;
        }

        @Override
        public int cost() {
            return cost;
        }

        @Override
        public String toString() {
            return FastStack.toString(a, aLen) + " | " + FastStack.toString(b, bLen) +": path=" + path + " cost=" + cost + " pushedToB=" + pushedToB;
        }

        @Override
        public Iterable<Operation> getOperations() {
            return OPERATIONS;
        }

        @Override
        public List<Operation> path() {
            return path;
        }

        @Override
        public boolean isRejected(Operation operation) {
            //TODO Could be optimized with a preprocessing step in validate and two lists constructor
            // A solution would be to create a int where bit i is set if opertaion with i=operation.ordinal is rejected
            if (PA == operation) {
                return pushedToB == 0 || PB == last;
            }
            if (PB == operation) {
                return aLen==0 || PA == last;
            }
            if (pushedToB < 2 && (SS == operation || SB == operation)) {
                return true;
            }
            if (aLen<2 && (SA == operation || SS == operation || RA == operation || RRA == operation)) {
                return true;
            }
            return operation.isNonOptimal(last);
        }

        @Override
        public <N extends Node<Operation>> N next(Operation op) {
            State result = new State(this);
            op.apply(result);
            result.cost = this.cost + 1;
            result.path = path;
            return (N) result;
        }

        @Override
        public void validate(Operation op) {
            List<Operation> newPath = new ArrayList<>(this.path.size()+1);
            newPath.addAll(this.path);
            newPath.add(op);
            last = op;
            this.path = newPath;
        }
    }

    public List<Operation> solve(int[] start, int[] target) {
        if (start.length!=target.length) {
            throw new IllegalArgumentException("Start and target arrays must have the same size");
        }
        if (start.length>=16) {
            throw new IllegalArgumentException("Start array must have less than 16 elements");
        }
        State targetState = new State(target, new int[0]);
        return super.solve(new State(start, new int[0]), targetState::equals);
    }
}
