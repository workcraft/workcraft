package org.workcraft.plugins.stg.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.stg.serialisation.LpnSerialiser;

public class LpnExporter extends AbstractSerialiseExporter {

    private final LpnSerialiser serialiser = new LpnSerialiser();

    @Override
    public LpnFormat getFormat() {
        return LpnFormat.getInstance();
    }

    @Override
    public LpnSerialiser getSerialiser() {
        return serialiser;
    }

}
