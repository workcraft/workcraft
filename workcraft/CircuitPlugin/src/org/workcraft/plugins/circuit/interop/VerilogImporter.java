package org.workcraft.plugins.circuit.interop;

public class VerilogImporter extends AbstractVerilogImporter {

    @SuppressWarnings("unused")  // Default constructor is required for PluginManager -- it is called via reflection.
    public VerilogImporter() {
        this(true, false);
    }

    public VerilogImporter(boolean celementAssign, boolean sequentialAssign) {
        super(celementAssign, sequentialAssign);
    }

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.DEFAULT;
    }

}
