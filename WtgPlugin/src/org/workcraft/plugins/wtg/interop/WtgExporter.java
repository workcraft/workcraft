package org.workcraft.plugins.wtg.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.wtg.serialisation.WtgSerialiser;

public class WtgExporter extends AbstractSerialiseExporter {

    WtgSerialiser serialiser = new WtgSerialiser();

    @Override
    public WtgFormat getFormat() {
        return WtgFormat.getInstance();
    }

    @Override
    public WtgSerialiser getSerialiser() {
        return serialiser;
    }

}
