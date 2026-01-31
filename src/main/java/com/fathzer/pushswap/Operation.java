package com.fathzer.pushswap;

public enum Operation {
    SA, SB, PA, PB, RA, RB, RR, RRA, RRB, RRR;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
