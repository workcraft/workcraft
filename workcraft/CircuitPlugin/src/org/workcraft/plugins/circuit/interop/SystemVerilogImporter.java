package org.workcraft.plugins.circuit.interop;

public class SystemVerilogImporter extends AbstractVerilogImporter {

    @SuppressWarnings("unused")  // Default constructor is required for PluginManager -- it is called via reflection.
    public SystemVerilogImporter() {
        this(true, false);
    }

    public SystemVerilogImporter(boolean celementAssign, boolean sequentialAssign) {
        super(celementAssign, sequentialAssign);
    }

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.SYSTEM_VERILOG_ASSIGN_STATEMENTS;
    }

}
