package org.workcraft.plugins.circuit.commands;

import java.util.Collection;

import org.workcraft.gui.graph.commands.AbstractStatisticsCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.util.MultiSet;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Circuit analysis";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        Collection<FunctionComponent> components = circuit.getFunctionComponents();
        Collection<Contact> ports = circuit.getPorts();

        int isolatedComponentCount = 0;
        int isolatedPinCount = 0;
        MultiSet<Integer> fanin = new MultiSet<>();
        MultiSet<Integer> fanout = new MultiSet<>();
        for (FunctionComponent component: components) {
            boolean isIsolatedComponent = true;
            Collection<Contact> inputs = component.getInputs();
            Collection<Contact> outputs = component.getOutputs();
            for (Contact input: inputs) {
                Contact driver = CircuitUtils.findDriver(circuit, input, false);
                if (driver == null) {
                    isolatedComponentCount++;
                } else {
                    isIsolatedComponent = false;
                }
            }
            fanin.add(inputs.size());
            for (Contact output: outputs) {
                Collection<Contact> driven = CircuitUtils.findDriven(circuit, output, false);
                if (driven.isEmpty()) {
                    isolatedPinCount++;
                } else {
                    isIsolatedComponent = false;
                }
                fanout.add(driven.size());
            }
            if (isIsolatedComponent) {
                isolatedComponentCount++;
            }
        }

        int inputPortCount = 0;
        int outputPortCount = 0;
        int isolatedPortCount = 0;
        for (Contact port: ports) {
            if (port.isOutput()) {
                outputPortCount++;
            } else {
                inputPortCount++;
                Collection<Contact> driven = CircuitUtils.findDriven(circuit, port, false);
                fanout.add(driven.size());
            }
            if (circuit.getPreset(port).isEmpty() && circuit.getPostset(port).isEmpty()) {
                isolatedPortCount++;
            }
        }

        return "Circuit analysis:"
                + "\n  Component count -  " + components.size()
                + "\n  Port count -  " + ports.size()
                + "\n    * Input / output -  " + inputPortCount + " / " + outputPortCount
                + "\n  Fanin distribution (0 / 1 / 2 ...) -  " + getDistribution(fanin)
                + "\n  Fanout distribution (0 / 1 / 2 ...) -  " + getDistribution(fanout)
                + "\n  Isolated components / ports / pins -  " + isolatedComponentCount + " / " + isolatedPortCount
                + " / " + isolatedPinCount;
    }

    private String getDistribution(MultiSet<Integer> multiset) {
        String result = "";
        int max = 0;
        for (Integer i: multiset.toSet()) {
            if (i > max) {
                max = i;
            }
        }
        for (int i = 0; i <= max; ++i) {
            if (!result.isEmpty()) {
                result += " / ";
            }
            result += multiset.count(i);
        }
        return result;
    }

}
