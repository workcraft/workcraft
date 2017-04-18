package org.workcraft.plugins.circuit.interop;

import java.util.UUID;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.circuit.serialisation.VerilogSerialiser;
import org.workcraft.serialisation.Format;

public class VerilogExporter extends AbstractSerialiseExporter {

    VerilogSerialiser serialiser = new VerilogSerialiser();

    @Override
    public UUID getTargetFormat() {
        return Format.VERILOG;
    }

    @Override
    public VerilogSerialiser getSerialiser() {
        return serialiser;
    }

}
