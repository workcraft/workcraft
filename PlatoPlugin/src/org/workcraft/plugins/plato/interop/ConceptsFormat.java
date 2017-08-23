package org.workcraft.plugins.plato.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class ConceptsFormat implements Format {

    private static ConceptsFormat instance = null;

    private ConceptsFormat() {
    }

    public static ConceptsFormat getInstance() {
        if (instance == null) {
            instance = new ConceptsFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("9c65e8ef-654f-4e59-88eb-e155a86db5bd");
    }

    @Override
    public String getName() {
        return "Concepts";
    }

    @Override
    public String getExtension() {
        return ".hs";
    }

    @Override
    public String getDescription() {
        return "Concepts file";
    }

}
