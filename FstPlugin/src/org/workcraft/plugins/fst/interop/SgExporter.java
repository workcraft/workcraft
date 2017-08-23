package org.workcraft.plugins.fst.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.fst.serialisation.SgSerialiser;

public class SgExporter extends AbstractSerialiseExporter {

    SgSerialiser serialiser = new SgSerialiser();

    @Override
    public Format getFormat() {
        return SgFormat.getInstance();
    }

    @Override
    public SgSerialiser getSerialiser() {
        return serialiser;
    }

}
