package org.workcraft.verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VerificationGoalTests {

    private final VerificationGoal goal = new VerificationGoal("test", "Property", we -> true);

    @Test
    void testShortExceptionsAreListed() {
        Assertions.assertEquals("Property", goal.getDescription(""));
        Assertions.assertEquals("Property with exceptions (a, b)", goal.getDescription("a\nb"));
        Assertions.assertEquals("Property with exceptions (a, b, c, d, e)", goal.getDescription("a\nb\nc\nd\ne"));
        // A single item beyond the limit is listed, as "and 1 more" is not shorter
        Assertions.assertEquals("Property with exceptions (a, b, c, d, e, f)",
                goal.getDescription("a\nb\nc\nd\ne\nf"));
    }

    @Test
    void testLongExceptionsAreShortened() {
        Assertions.assertEquals("Property with exceptions (a, b, c, d, e and 2 more)",
                goal.getDescription("a\nb\nc\nd\ne\nf\ng"));
    }

}
