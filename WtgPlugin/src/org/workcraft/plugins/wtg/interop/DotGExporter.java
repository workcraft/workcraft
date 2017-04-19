package org.workcraft.plugins.wtg.interop;

import java.util.UUID;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.wtg.serialisation.DotGSerialiser;
import org.workcraft.serialisation.Format;

public class DotGExporter extends AbstractSerialiseExporter {

    DotGSerialiser serialiser = new DotGSerialiser();

    @Override
    public UUID getTargetFormat() {
        return Format.WTG;
    }

    @Override
    public DotGSerialiser getSerialiser() {
        return serialiser;
    }

}
