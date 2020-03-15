package org.workcraft.plugins.dfs.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.dfs.serialisation.VerilogSerialiser;

public class VerilogExporter extends AbstractSerialiseExporter {

    private final VerilogSerialiser serialiser = new VerilogSerialiser();

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public VerilogSerialiser getSerialiser() {
        return serialiser;
    }

}
