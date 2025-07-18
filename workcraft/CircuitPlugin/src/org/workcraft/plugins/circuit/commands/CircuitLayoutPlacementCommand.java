package org.workcraft.plugins.circuit.commands;

public class CircuitLayoutPlacementCommand extends CircuitLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Circuit placement only";
    }

    @Override
    public boolean skipLayoutRouting() {
        return true;
    }

}
