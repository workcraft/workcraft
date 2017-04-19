package org.workcraft.plugins.circuit.interop;

import java.util.UUID;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.circuit.serialisation.SdcSerialiser;
import org.workcraft.serialisation.Format;

public class SdcExporter extends AbstractSerialiseExporter {

    SdcSerialiser serialiser = new SdcSerialiser();

    @Override
    public UUID getTargetFormat() {
        return Format.SDC;
    }

    @Override
    public SdcSerialiser getSerialiser() {
        return serialiser;
    }

}
