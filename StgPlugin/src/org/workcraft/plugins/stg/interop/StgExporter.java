package org.workcraft.plugins.stg.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.stg.serialisation.AstgSerialiser;

public class StgExporter extends AbstractSerialiseExporter {

    AstgSerialiser serialiser = new AstgSerialiser();

    @Override
    public Format getFormat() {
        return StgFormat.getInstance();
    }

    @Override
    public AstgSerialiser getSerialiser() {
        return serialiser;
    }

}
