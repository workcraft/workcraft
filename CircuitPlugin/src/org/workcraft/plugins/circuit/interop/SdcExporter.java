package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.circuit.serialisation.SdcSerialiser;

public class SdcExporter extends AbstractSerialiseExporter {

    SdcSerialiser serialiser = new SdcSerialiser();

    @Override
    public SdcFormat getFormat() {
        return SdcFormat.getInstance();
    }

    @Override
    public SdcSerialiser getSerialiser() {
        return serialiser;
    }

}
