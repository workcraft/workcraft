package org.workcraft.plugins.circuit.commands;

public class CircuitLayoutRoutingCommand extends CircuitLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Circuit routing only";
    }

    @Override
    public boolean skipLayoutPlacement() {
        return true;
    }

}
