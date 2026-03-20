package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.types.Pair;

import java.util.*;

class SetUtilsTests {

    public static final Set<Integer> NUM_SET_1 = Set.of(1, 2, 3, 4);
    public static final Set<Integer> NUM_SET_2 = Set.of(2, 3, 4, 5);
    public static final Set<String> STRING_SET_1 = Set.of("a", "bb", "ccc");
    public static final Set<String> STRING_SET_2 = Set.of("bb", "ccc", "dddd");

    @Test
    void intersectionTest() {
        Assertions.assertEquals(NUM_SET_1,
                SetUtils.intersection(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(Set.of(2, 3, 4),
                SetUtils.intersection(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(Set.of("bb", "ccc"),
                SetUtils.intersection(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void unionTest() {
        Assertions.assertEquals(NUM_SET_1,
                SetUtils.union(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(Set.of(1, 2, 3, 4, 5),
                SetUtils.union(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(Set.of("a", "bb", "ccc", "dddd"),
                SetUtils.union(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void differenceTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.difference(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(Set.of(1),
                SetUtils.difference(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(Set.of("a"),
                SetUtils.difference(STRING_SET_1, STRING_SET_2));

        Assertions.assertEquals(Set.of("dddd"),
                SetUtils.difference(STRING_SET_2, STRING_SET_1));
    }

    @Test
    void symmetricDifferenceTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.symmetricDifference(NUM_SET_1, NUM_SET_1));

        Assertions.assertEquals(Set.of(1, 5),
                SetUtils.symmetricDifference(NUM_SET_1, NUM_SET_2));

        Assertions.assertEquals(Set.of("a", "dddd"),
                SetUtils.symmetricDifference(STRING_SET_1, STRING_SET_2));
    }

    @Test
    void isFirstSmallerTest() {
        Assertions.assertFalse(SetUtils.isFirstSmaller(NUM_SET_1, NUM_SET_2, false));
        Assertions.assertFalse(SetUtils.isFirstSmaller(STRING_SET_1, STRING_SET_2, true));
        Assertions.assertTrue(SetUtils.isFirstSmaller(Set.of("a"), STRING_SET_1, false));
    }

    @Test
    void convertArraysToSetsTest() {
        Assertions.assertEquals(new HashSet<>(),
                SetUtils.convertArraysToSets(new Object[][]{}));

        Set<String> abSet = Set.of("a", "b");
        Set<String> abcSet = Set.of("a", "b", "c");
        Assertions.assertEquals(Set.of(abSet, abcSet),
                SetUtils.convertArraysToSets(new String[][]{{"a", "b"}, {"b", "c", "a"}}));

        Assertions.assertEquals(Set.of(abSet, abcSet),
                SetUtils.convertArraysToSets(new String[][]{{"a", "b", "a", "b"}, {"b", "a", "c", "a"}}));

    }

    @Test
    void partitionTest() {
        Assertions.assertEquals(Pair.of(Collections.emptySet(), Collections.emptySet()),
                SetUtils.partition(new HashSet<>(), item -> true));

        Set<Integer> oddSet = Set.of(1, 3, 5);
        Set<Integer> evenSet = Set.of(2, 4, 6);
        List<Integer> ints = new ArrayList<>();
        ints.addAll(oddSet);
        ints.addAll(evenSet);
        Assertions.assertEquals(Pair.of(evenSet, oddSet),
                SetUtils.partition(ints, item -> item % 2 == 0));

        Set<String> shortSet = Set.of("", "a", "ab");
        Set<String> longSet = Set.of("qwerty", "asdfgh", "zxcvbn");
        List<String> strings = new ArrayList<>();
        strings.addAll(shortSet);
        strings.addAll(longSet);
        Assertions.assertEquals(Pair.of(shortSet, longSet),
                SetUtils.partition(strings, item -> item.length() < 3));
    }

    @Test
    void duplicatesTest() {
        Assertions.assertEquals(Collections.emptySet(),
                SetUtils.duplicates(Collections.emptyList()));

        Assertions.assertEquals(Collections.emptySet(),
                SetUtils.duplicates(List.of(1, 2, 3, 4, 5)));

        Assertions.assertEquals(Set.of(1, 2),
                SetUtils.duplicates(List.of(1, 2, 3, 1, 4, 2, 5, 2)));

        Assertions.assertEquals(Set.of("a", "b"),
                SetUtils.duplicates(List.of("a", "b", "a", "c", "b", "d", "b")));
    }

}
