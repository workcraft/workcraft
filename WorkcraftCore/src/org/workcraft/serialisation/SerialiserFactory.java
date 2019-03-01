package org.workcraft.serialisation;

public interface SerialiserFactory {
    XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException;
}
