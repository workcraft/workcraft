package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ListUtilsTests {

    @Test
    void zipTest() {
        Assertions.assertEquals(List.of(),
                ListUtils.zip(List.of(), List.of()));

        Assertions.assertEquals(List.of(1),
                ListUtils.zip(List.of(1), List.of(), 2));

        Assertions.assertEquals(List.of(2, 3, 4),
                ListUtils.zip(List.of(), List.of(2, 3, 4), 2));

        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4, 5),
                ListUtils.zip(Arrays.asList(1, 3, 5), Arrays.asList(2, 4), 1));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4"),
                        Arrays.asList("b1", "b2", "b3", "b4"), 2));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4", "b5", "b6", "b7"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4"),
                        Arrays.asList("b1", "b2", "b3", "b4", "b5", "b6", "b7"), 2));

        Assertions.assertEquals(Arrays.asList("a1", "a2", "b1", "b2", "a3", "a4", "b3", "b4", "a5", "a6", "a7"),
                ListUtils.zip(Arrays.asList("a1", "a2", "a3", "a4", "a5", "a6", "a7"),
                        Arrays.asList("b1", "b2", "b3", "b4"), 2));
    }

    @Test
    void permutateTest() {
        Assertions.assertEquals(List.of(List.of()),
                ListUtils.permutate(List.of()));

        Assertions.assertEquals(List.of(List.of(true, false), List.of(false, true)),
                ListUtils.permutate(List.of(true, false)));

        Assertions.assertEquals(List.of(
                        List.of("a", "b", "c"), List.of("b", "a", "c"), List.of("b", "c", "a"),
                        List.of("a", "c", "b"),  List.of("c", "a", "b"), List.of("c", "b", "a")),
                ListUtils.permutate(List.of("a", "b", "c")));

    }

    @Test
    void combileTest() {
        Assertions.assertEquals(List.of(List.of()),
                ListUtils.combine(List.of(), 0));

        Assertions.assertEquals(List.of(List.of()),
                ListUtils.combine(List.of(true, false), 0));

        Assertions.assertEquals(List.of(List.of(true), List.of(false)),
                ListUtils.combine(List.of(true, false), 1));

        Assertions.assertEquals(List.of(List.of(true, true), List.of(false, true), List.of(true, false), List.of(false, false)),
                ListUtils.combine(List.of(true, false), 2));

        Assertions.assertEquals(List.of(
                        List.of("a", "a", "a"), List.of("b", "a", "a"), List.of("c", "a", "a"),
                        List.of("a", "b", "a"), List.of("b", "b", "a"), List.of("c", "b", "a"),
                        List.of("a", "c", "a"), List.of("b", "c", "a"), List.of("c", "c", "a"),
                        List.of("a", "a", "b"), List.of("b", "a", "b"), List.of("c", "a", "b"),
                        List.of("a", "b", "b"), List.of("b", "b", "b"), List.of("c", "b", "b"),
                        List.of("a", "c", "b"), List.of("b", "c", "b"), List.of("c", "c", "b"),
                        List.of("a", "a", "c"), List.of("b", "a", "c"), List.of("c", "a", "c"),
                        List.of("a", "b", "c"), List.of("b", "b", "c"), List.of("c", "b", "c"),
                        List.of("a", "c", "c"), List.of("b", "c", "c"), List.of("c", "c", "c")),
                ListUtils.combine(List.of("a", "b", "c"), 3));

        Assertions.assertEquals(256,
                ListUtils.combine(List.of(true, false), 8).size());

        Assertions.assertEquals(625,
                ListUtils.combine(List.of(1, 2, 3, 4, 5), 4).size());
    }

}
