package com.fathzer.pushswap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotationTest {

    @Test
    void testCost() {
        Stack stackA = new Stack(new int[]{6,0,4});
        Stack stackB = new Stack(new int[]{3,1,2,8,7,5,9});
        Rotation rotation = new Rotation();

        rotation.cheapest(stackB, 0, stackA, 2);
        Rotation expectedRotation = new Rotation();
        expectedRotation.rra = 1;
        assertEquals(expectedRotation, rotation);

        // A more complex case where best way is not to use ra but only rrr and rb because
        // with backward rotations, we meet the target element earlier, without having to move
        // the small stack alone
        stackA = new Stack(new int[]{8,14,16,0,4});
        stackB = new Stack(new int[]{5,1,2,11,10,3,7,9,15,18,20,21});
        rotation.cheapest(stackB, 8, stackA, 2);
        int cost = rotation.cost();
        clear(expectedRotation);
        expectedRotation.rrr = 3;
        expectedRotation.rrb = 1;
        assertEquals(expectedRotation, rotation);
        assertEquals(4, cost);
    }

    private void clear(Rotation rotation) {
        rotation.rb = 0;
        rotation.rrb = 0;
        rotation.ra = 0;
        rotation.rra = 0;
        rotation.rr = 0;
        rotation.rrr = 0;
    }
}
