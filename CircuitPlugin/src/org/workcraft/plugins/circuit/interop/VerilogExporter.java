package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.circuit.serialisation.VerilogSerialiser;

public class VerilogExporter extends AbstractSerialiseExporter {

    VerilogSerialiser serialiser = new VerilogSerialiser();

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public VerilogSerialiser getSerialiser() {
        return serialiser;
    }

}
