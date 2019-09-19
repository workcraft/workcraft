package org.workcraft.serialisation.reflection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ConstructorParametersMatcher {

    private static class ConstructorInfo<T> implements MethodParametersMatcher.MethodInfo {
        ConstructorInfo(Constructor<? extends T> constructor) {
            this.constructor = constructor;
            this.parameterTypes = constructor.getParameterTypes();
        }
        public final Constructor<? extends T> constructor;
        private final Class<?>[] parameterTypes;

        @Override
        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<? extends T> match(Class<? extends T> c, Class<?>... parameters) throws NoSuchMethodException {
        ArrayList<ConstructorInfo<T>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : c.getConstructors()) {
            constructors.add(new ConstructorInfo<T>((Constructor<? extends T>) constructor));
        }

        try {
            return MethodParametersMatcher.match(constructors, parameters).constructor;
        } catch (NoSuchMethodException e) {
            String s = "";
            for (Class<?> parameter : parameters) {
                if (s.length() > 0) {
                    s += ", ";
                }
                s += parameter.getCanonicalName();
            }
            throw new NoSuchMethodException("Unable to find a constructor for class " + c.getCanonicalName() + " with parameters (" + s + ")");
        }
    }

}
