package org.workcraft.plugins.circuit.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class VerilogFormat implements Format {

    private static VerilogFormat instance = null;

    private VerilogFormat() {
    }

    public static VerilogFormat getInstance() {
        if (instance == null) {
            instance = new VerilogFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("fdd4414e-fd02-4702-b143-09b24430fdd1");
    }

    @Override
    public String getName() {
        return "Verilog";
    }

    @Override
    public String getExtension() {
        return ".v";
    }

    @Override
    public String getDescription() {
        return "Verilog netlist";
    }

}
