package org.workcraft.plugins.fst.interop;

import java.util.UUID;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.fst.serialisation.DotGSerialiser;
import org.workcraft.serialisation.Format;

public class DotGExporter extends AbstractSerialiseExporter {

    DotGSerialiser serialiser = new DotGSerialiser();

    @Override
    public UUID getTargetFormat() {
        return Format.SG;
    }

    @Override
    public DotGSerialiser getSerialiser() {
        return serialiser;
    }

}
