package com.fathzer.pushswap.buterfly;

import com.fathzer.pushswap.IStack;

interface BPusher {
    enum Command {
        TO_BOTTOM,
        TO_TOP,
        KEEP
    }

    Command evaluate(int value);

    boolean isNotEnded(IStack a);
}
