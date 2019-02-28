package org.workcraft.serialisation;

interface DeserialiserFactory {
    XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException;
}
