package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.xbm.serialisation.BmSerialiser;

public class BmExporter extends AbstractSerialiseExporter {

    private final BmSerialiser serialiser = new BmSerialiser();

    @Override
    public Format getFormat() {
        return BmFormat.getInstance();
    }

    @Override
    public BmSerialiser getSerialiser() {
        return serialiser;
    }
}
