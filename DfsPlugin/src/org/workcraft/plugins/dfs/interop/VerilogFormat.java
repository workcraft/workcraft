package org.workcraft.plugins.dfs.interop;

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
        return UUID.fromString("4c6c79c1-70b7-4629-869b-b8228cc711b4");
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
