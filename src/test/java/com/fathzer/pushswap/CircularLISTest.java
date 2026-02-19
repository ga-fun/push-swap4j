package com.fathzer.pushswap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.fathzer.pushswap.LIS.getCircular;

import java.util.BitSet;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CircularLISTest {
    @ParameterizedTest
    @MethodSource("testCases")
    void test(int[] input, int expectedCardinality) {
        BitSet result = getCircular(input);
		assertEquals(expectedCardinality, result.cardinality(), "Wrong result for "+input+". result is "+result);
    }

    private static Stream<Arguments> testCases() {
        return Stream.of(
            Arguments.of(new int[] {1, 2, 0}, 3),
            Arguments.of(new int[] {3, 1, 2, 0}, 3),
            Arguments.of(new int[] {2, 3, 4, 0, 1}, 5),
            Arguments.of(new int[] {4, 3, 2, 1, 0}, 2),
            Arguments.of(new int[] {0, 1, 2, 3, 4}, 5),
            Arguments.of(new int[] {1, 2, 0, 3, 4}, 4)
        );
    }

}
