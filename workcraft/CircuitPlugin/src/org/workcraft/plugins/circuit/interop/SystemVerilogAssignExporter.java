package org.workcraft.plugins.circuit.interop;

public class SystemVerilogAssignExporter extends AbstractVerilogExporter {

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.SYSTEM_VERILOG_ASSIGN_STATEMENTS;
    }

}
