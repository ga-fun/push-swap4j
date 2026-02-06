package com.fathzer.pushswap;

import java.util.Objects;

/** A class that is able to find optimal rotations for pushing elements from B to A */
public class Rotation {
    int ra;
    int rra;
    int rb;
    int rrb;
    int rr;
    int rrr;

    public int cost() {
        return ra + rra + rb + rrb + rr + rrr;
    }

    public void cheapest(Stack stackB, int indexInB, Stack stackA, int targetIndexA) {
        int stackBSize = stackB.size();
        int stackASize = stackA.size();

        this.clear();

        // 1. Calcul des distances individuelles
        int distRB = indexInB;
        int distRRB = stackBSize - indexInB;
        int distRA = targetIndexA;
        int distRRA = stackASize - targetIndexA;

        // 2. Calcul des coûts pour les 4 scénarios possibles
        int costRR = Math.max(distRB, distRA);
        int costRRR = Math.max(distRRB, distRRA);
        int costRaRrb = distRA + distRRB;
        int costRraRb = distRRA + distRB;

        // 3. Trouver le coût minimum
        int min = Math.min(Math.min(costRR, costRRR), Math.min(costRaRrb, costRraRb));

        // 4. Appliquer la meilleure stratégie
        if (min == costRR) {
            this.rr = Math.min(distRB, distRA);
            this.rb = distRB - rr;
            this.ra = distRA - rr;
        } else if (min == costRRR) {
            this.rrr = Math.min(distRRB, distRRA);
            this.rrb = distRRB - rrr;
            this.rra = distRRA - rrr;
        } else if (min == costRaRrb) {
            this.ra = distRA;
            this.rrb = distRRB;
        } else { // costRRA_RB
            this.rra = distRRA;
            this.rb = distRB;
        }
    }

    public void clear() {
        this.ra = 0;
        this.rra = 0;
        this.rb = 0;
        this.rrb = 0;
        this.rr = 0;
        this.rrr = 0;
    }

    @Override
    public String toString() {
        return "{" +
                "ra=" + ra +
                ", rra=" + rra +
                ", rb=" + rb +
                ", rrb=" + rrb +
                ", rr=" + rr +
                ", rrr=" + rrr +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rotation rotation = (Rotation) obj;
        return ra == rotation.ra && rra == rotation.rra && rb == rotation.rb && rrb == rotation.rrb && rr == rotation.rr && rrr == rotation.rrr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ra, rra, rb, rrb, rr, rrr);
    }
}
