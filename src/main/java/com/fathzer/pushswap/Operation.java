package com.fathzer.pushswap;

import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public enum Operation {
    SA(PushSwapManager::sa),
    SB(PushSwapManager::sb),
    SS(PushSwapManager::ss),
    PA(PushSwapManager::pa),
    PB(PushSwapManager::pb),
    RA(PushSwapManager::ra),
    RB(PushSwapManager::rb),
    RR(PushSwapManager::rr),
    RRA(PushSwapManager::rra),
    RRB(PushSwapManager::rrb),
    RRR(PushSwapManager::rrr);

    private final Consumer<PushSwapManager> action;
    private static final EnumMap<Operation, Predicate<Operation>> isNonOptimal = new EnumMap<>(Operation.class);

    static {
        isNonOptimal.put(SA, o -> o == SA || o == SS || o == SB);
        isNonOptimal.put(SB, o -> o == SB || o == SS || o == SA);
        isNonOptimal.put(SS, o -> o == SS || o == SA || o == SB);
        isNonOptimal.put(PA, o -> o == PB);
        isNonOptimal.put(PB, o -> o == PA);
        isNonOptimal.put(RA, o -> o == RB || o == RRA || o == RRR);
        isNonOptimal.put(RB, o -> o == RA || o == RRB || o == RRR);
        isNonOptimal.put(RR, o -> o == RRA || o == RRB || o == RRR);
        isNonOptimal.put(RRA, o -> o == RA || o == RRB || o == RR);
        isNonOptimal.put(RRB, o -> o == RB || o == RRA || o == RR);
        isNonOptimal.put(RRR, o -> o == RA || o == RB || o == RR);
    }

    private Operation(Consumer<PushSwapManager> action) {
        this.action = action;
    }

    /**
     * Applies this operation to the given push_swap manager.
     * @param manager The push_swap manager to apply the operation to
     */
    public void apply(PushSwapManager manager) {
        action.accept(manager);
    }

    /**
     * Checks if this operation is non-optimal when followed by another operation.
     * @param other The next operation to check against
     * @return true if this operation is non-optimal when followed by the other operation
     */
    public boolean isNonOptimal(Operation other) {
        return isNonOptimal.get(this).test(other);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
