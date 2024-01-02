package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public enum VerilogFormat implements Format {

    DEFAULT("fdd4414e-fd02-4702-b143-09b24430fdd1",
            "Verilog",
            "Verilog netlist",
            false),

    ASSIGN_STATEMENTS("f88c58f1-5be6-4d78-96d5-1f6581cac4ec",
            "Verilog assigns",
            "Verilog netlist using assign statements",
            true);

    private final UUID uuid;
    private final String name;
    private final String description;
    private final boolean assignOnly;

    VerilogFormat(String uuidString, String name, String description, boolean assignOnly) {
        uuid = UUID.fromString(uuidString);
        this.name = name;
        this.description = description;
        this.assignOnly = assignOnly;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExtension() {
        return ".v";
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean useAssignOnly() {
        return assignOnly;
    }

}
