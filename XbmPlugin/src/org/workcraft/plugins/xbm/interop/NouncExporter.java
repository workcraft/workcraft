package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.xbm.serialisation.NouncSerialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class NouncExporter extends AbstractSerialiseExporter {

    private final NouncSerialiser serialiser = new NouncSerialiser();

    @Override
    public Format getFormat() {
        return NouncFormat.getInstance();
    }

    @Override
    public ModelSerialiser getSerialiser() {
        return serialiser;
    }
}
