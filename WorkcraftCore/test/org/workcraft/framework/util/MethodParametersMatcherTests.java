package org.workcraft.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.util.AmbiguousMethodException;
import org.workcraft.util.MethodParametersMatcher;
import org.workcraft.util.MethodParametersMatcher.MethodInfo;

public class MethodParametersMatcherTests {
    class A {
    }

    class AB extends A {
    }

    class AC extends A {
    }

    class ABq extends AB {
    }

    class ABp extends AB {
    }

    int match(Class<?> type, Class<?>... parameters) throws Exception {
        TestMethodInfo match;
        try {
            match = MethodParametersMatcher.match(getMethods(type), parameters);
        } catch (NoSuchMethodException e) {
            return -1;
        } catch (AmbiguousMethodException e) {
            return -2;
        }
        return match.execute();
    }

    static class Simple {
        public static int qq() {
            return 1;
        }
    }

    @Test
    public void testSimple() throws Exception {
        Assert.assertEquals(1, match(Simple.class));
        Assert.assertEquals(-1, match(Simple.class, Object.class));
    }

    static class Advanced {
        public static int qq() {
            return 1;
        }
        public static int qq(A a) {
            return 2;
        }
        public static int qq(ABq abq) {
            return 3;
        }
        public static int qq(AB ab) {
            return 4;
        }
        public static int qq(AC ac) {
            return 5;
        }
    }

    @Test
    public void testMostSpecific() throws Exception {
        Assert.assertEquals(1, match(Advanced.class));
        Assert.assertEquals(2, match(Advanced.class, A.class));
        Assert.assertEquals(3, match(Advanced.class, ABq.class));
        Assert.assertEquals(4, match(Advanced.class, ABp.class));
        Assert.assertEquals(4, match(Advanced.class, AB.class));
        Assert.assertEquals(5, match(Advanced.class, AC.class));
    }

    static class Ambiguous {
        public static int qq(A a, AC b) {
            return 1;
        }
        public static int qq(ABq a, A b) {
            return 2;
        }
    }

    @Test
    public void testAmbiguous() throws Exception {
        Assert.assertEquals(1, match(Ambiguous.class, A.class, AC.class));
        Assert.assertEquals(-1, match(Ambiguous.class, AB.class, A.class));
        Assert.assertEquals(2, match(Ambiguous.class, ABq.class, A.class));
        Assert.assertEquals(1, match(Ambiguous.class, AB.class, AC.class));
        Assert.assertEquals(-2, match(Ambiguous.class, ABq.class, AC.class));
    }

    class TestMethodInfo implements MethodInfo {
        private final Method method;

        TestMethodInfo(Method method) {
            this.method = method;
        }

        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        public int execute() throws Exception {
            Object[] args = new Object[getParameterTypes().length];
            return (Integer) method.invoke(null, args);
        }
    }

    private Collection<TestMethodInfo> getMethods(Class<?> clasz) {
        ArrayList<TestMethodInfo> result = new ArrayList<>();

        for (Method method : clasz.getMethods()) {
            if (method.getName() == "qq") {
                result.add(new TestMethodInfo(method));
            }
        }
        return result;
    }

}
