package com.fathzer.pushswap.butterfly;

import com.fathzer.pushswap.AbstractPushSwapSorter;

/**
 * This class is used to delay some operations on A in order to find some operations on B that can be grouped with delayed ones.
 * <br>For example, if sa is required on A, and, before any other operation on A we have a sb, we can replace both by ss.
 */
class DelayedOperations {
    private final AbstractPushSwapSorter sorter;
    private boolean saRequired;
    private int rraRequired;
    
    DelayedOperations(AbstractPushSwapSorter manager) {
        this.sorter = manager;
    }

    int elementOfA(int index) {
        if (saRequired) {
            if (index==0) {
                index = 1;
            } else if (index==1) {
                index = 0;
            }
        }
        int size = sorter.getAStack().size();
        int virtualIndex = (index + size - rraRequired)%size;
        return sorter.getAStack().get(virtualIndex);
    }

    int headOfA() {
        return elementOfA(0);
    }

    int tailOfA() {
        int size = sorter.getAStack().size();
        return elementOfA(size-1);
    }

    void ra() {
        doPendingSa();
        rraRequired--;
    }
    
    void rra() {
        doPendingSa();
        rraRequired++;
    }

    void sa() {
        doPendingRotations();
        saRequired = true;
    }

    void pa() {
        // Perform pending operations before pushing from B to A
        processPending();
        sorter.pa();
    }

    void sb() {
        if (saRequired) {
            sorter.ss();
            saRequired = false;
        } else {
            sorter.sb();
        }
    }

    void rb() {
        if (rraRequired<0) {
            doPendingSa();
            rraRequired++;
            sorter.rr();
        } else {
            sorter.rb();
        }
    }

    void rrb() {
        if (rraRequired>0) {
            doPendingSa();
            rraRequired--;
            sorter.rrr();
        } else {
            sorter.rrb();
        }
    }

    void processPending() {
        doPendingSa();
        doPendingRotations();
    }

    boolean isSaPending() {
        return saRequired;
    }

    private void doPendingSa() {
        if (saRequired) {
            sorter.sa();
            saRequired = false;
        }
    }

    private void doPendingRotations() {
        while (rraRequired > 0) {
            sorter.rra();
            rraRequired--;
        }
        while (rraRequired < 0) {
            sorter.ra();
            rraRequired++;
        }
    }
}
