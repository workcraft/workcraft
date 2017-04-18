package org.workcraft.plugins.stg.interop;

import java.util.UUID;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.stg.serialisation.LpnSerialiser;
import org.workcraft.serialisation.Format;

public class LpnExporter extends AbstractSerialiseExporter {

    LpnSerialiser serialiser = new LpnSerialiser();

    @Override
    public UUID getTargetFormat() {
        return Format.LPN;
    }

    @Override
    public LpnSerialiser getSerialiser() {
        return serialiser;
    }

}
