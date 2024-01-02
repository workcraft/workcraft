package org.workcraft.plugins.circuit.interop;

public class VerilogAssignExporter extends AbstractVerilogExporter {

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.ASSIGN_STATEMENTS;
    }

}
