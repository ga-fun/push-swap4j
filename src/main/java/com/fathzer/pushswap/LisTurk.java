package com.fathzer.pushswap;

import com.fathzer.pushswap.pusher.AbstractPusher;
import com.fathzer.pushswap.pusher.AtoBAllButLisPusher;

public class LisTurk extends Turk {
    public LisTurk(int[] numbers) {
        super(numbers);
    }

    @Override
    protected AbstractPusher getFirstPhasePusher() {
        return new AtoBAllButLisPusher(this);
    }
}
