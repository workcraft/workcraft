package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public enum VerilogFormat implements Format {

    DEFAULT("fdd4414e-fd02-4702-b143-09b24430fdd1",
            "Verilog",
            ".v",
            "Verilog netlist",
            false,
            false),

    SYSTEM_VERILOG_ASSIGN_STATEMENTS("f88c58f1-5be6-4d78-96d5-1f6581cac4ec",
            "System Verilog assigns",
            ".sv",
            "System Verilog with assign statements",
            true,
            true);

    private final UUID uuid;
    private final String name;
    private final String extension;
    private final String description;
    private final boolean assignOnly;
    private final boolean systemVerilogSyntax;

    VerilogFormat(String uuidString, String name, String extension, String description, boolean assignOnly,
            boolean systemVerilogSyntax) {

        uuid = UUID.fromString(uuidString);
        this.name = name;
        this.extension = extension;
        this.description = description;
        this.assignOnly = assignOnly;
        this.systemVerilogSyntax = systemVerilogSyntax;
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
        return extension;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean useAssignOnly() {
        return assignOnly;
    }

    public boolean useSystemVerilogSyntax() {
        return systemVerilogSyntax;
    }

}
