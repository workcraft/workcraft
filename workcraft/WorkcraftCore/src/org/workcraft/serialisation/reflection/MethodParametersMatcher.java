package org.workcraft.serialisation.reflection;

import java.util.ArrayList;
import java.util.Collection;

public final class MethodParametersMatcher<T extends MethodParametersMatcher.MethodInfo> {

    private MethodParametersMatcher() {
    }

    public interface MethodInfo {
        Class<?>[] getParameterTypes();
    }

    private ArrayList<T> methods;

    public static <T extends MethodParametersMatcher.MethodInfo> T match(Collection<T> methods, Class<?>... parameters) throws NoSuchMethodException {
        MethodParametersMatcher<T> matcher = new MethodParametersMatcher<>();
        return matcher.instanceMatch(methods, parameters);
    }

    private T instanceMatch(Collection<T> methods, Class<?>... parameters) throws NoSuchMethodException {
        this.methods = new ArrayList<>(methods);

        matchByParameters(parameters);

        filtered = new boolean[this.methods.size()];
        for (int i = 0; i < parameters.length; i++) {
            filterByParameter(i);
        }

        for (int i = this.methods.size(); --i >= 0;) {
            if (filtered[i]) {
                remove(i);
            }
        }

        if (this.methods.size() > 1) {
            throw new RuntimeException("We have a bug o_O");
        }

        if (this.methods.isEmpty()) {
            if (filtered.length > 1) {
                throw new AmbiguousMethodException();
            } else {
                if (filtered.length == 0) {
                    throw new NoSuchMethodException("Constructor not found");
                } else {
                    throw new RuntimeException("We have a bug o_O");
                }
            }
        }

        return this.methods.get(0);
    }

    private void matchByParameters(Class<?>[] parameters) {
        for (int i = methods.size(); --i >= 0;) {
            if (!matches(methods.get(i).getParameterTypes(), parameters)) {
                remove(i);
            }
        }
    }

    private boolean matches(Class<?>[] actual, Class<?>[] expected) {
        if (expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (!actual[i].isAssignableFrom(expected[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean[] filtered;

    private void filterByParameter(int parameterNumber) {
        Class<?> best = null;

        for (int i = methods.size(); --i >= 0;) {
            Class<?> current = methods.get(i).getParameterTypes()[parameterNumber];
            if (best == null || best.isAssignableFrom(current)) {
                best = current;
            }
        }
        for (int i = methods.size(); --i >= 0;) {
            if (methods.get(i).getParameterTypes()[parameterNumber] != best) {
                filtered[i] = true;
            }
        }
    }

    private void remove(int i) {
        int last = methods.size() - 1;
        methods.set(i, methods.get(last));
        methods.remove(last);
    }

}
