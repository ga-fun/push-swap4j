package com.fathzer.pushswap.butterfly;

import java.util.BitSet;
import java.util.function.IntFunction;

import com.fathzer.pushswap.IStack;
import com.fathzer.pushswap.LIS;

class AToBLisPusherConfig {
    private boolean circularLis;
    IntFunction<Integer> windowSizeBuilder;

    public AToBLisPusherConfig() {
        this.circularLis = true;
        // windowSize définit la largeur de la fenêtre glissante.
        // Coéfficients empiriques (marche bien pour 500 éléments).
        this.windowSizeBuilder = size -> (int) (Math.sqrt(size) * 1.6);
    }

    public AToBLisPusherConfig setCircularLis(boolean circularLis) {
        this.circularLis = circularLis;
        return this;
    }

    public AToBLisPusherConfig setWindowSizeBuilder(IntFunction<Integer> windowSizeBuilder) {
        this.windowSizeBuilder = windowSizeBuilder;
        return this;
    }

    public BitSet getLis(IStack stackA) {
        int[] array = stackA.toArray();
        return this.circularLis ? LIS.getCircular(array) : LIS.get(array, 0);
    }
}