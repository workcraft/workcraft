package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.xbm.serialisation.BmSerialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class BmExporter extends AbstractSerialiseExporter {

    final BmSerialiser serialiser = new BmSerialiser();

    @Override
    public Format getFormat() {
        return BmFormat.getInstance();
    }

    @Override
    public BmSerialiser getSerialiser() {
        return serialiser;
    }
}
