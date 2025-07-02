package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.types.MultiSet;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class StatisticsCommand extends AbstractStatisticsCommand {

    private static final int MAX_DISTRIBUTION = 9;

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
        for (FunctionComponent component : components) {
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

        int inPortCount = 0;
        int outPortCount = 0;
        int isolatedPortCount = 0;
        for (Contact port: ports) {
            if (port.isOutput()) {
                outPortCount++;
            } else {
                inPortCount++;
                Collection<Contact> driven = CircuitUtils.findDriven(circuit, port, false);
                fanout.add(driven.size());
            }
            if (circuit.getPreset(port).isEmpty() && circuit.getPostset(port).isEmpty()) {
                isolatedPortCount++;
            }
        }

        double gateArea = 0.0;
        int mappedCount = 0;
        Library library = LibraryManager.getLibrary();
        MultiSet<String> namedComponents = new MultiSet<>();
        for (FunctionComponent component : components) {
            if (component.isMapped()) {
                mappedCount++;
                String moduleName = component.getModule();
                Gate gate = library.get(moduleName);
                if (gate != null) {
                    gateArea += gate.size;
                } else {
                    namedComponents.add(moduleName);
                }
            }
        }
        int unmappedCount = components.size() - mappedCount;

        int bufCount = 0;
        int bufZeroDelayCount = 0;
        int invCount = 0;
        int invZeroDelayCount = 0;
        int constCount = 0;
        int blackboxCount = 0;
        int otherCount = 0;
        for (FunctionComponent component : components) {
            if (component.isConst()) {
                constCount++;
            } else if (component.isBuffer()) {
                bufCount++;
                if (component.getIsZeroDelay()) {
                    bufZeroDelayCount++;
                }
            } else if (component.isInverter()) {
                invCount++;
                if (component.getIsZeroDelay()) {
                    invZeroDelayCount++;
                }
            } else if (component.isBlackbox()) {
                blackboxCount++;
            } else {
                otherCount++;
            }
        }
        int trivialCount = bufCount + invCount + constCount;
        int nontrivialCount = otherCount + blackboxCount;

        int combCount = 0;
        int seqCount = 0;
        int undefinedCount = 0;
        int combLiteralCount = 0;
        int seqSetLiteralCount = 0;
        int seqResetLiteralCount = 0;
        for (FunctionComponent component : components) {
            for (FunctionContact contact : component.getFunctionContacts()) {
                if (!contact.isOutput()) continue;
                if (contact.getSetFunction() == null) {
                    undefinedCount++;
                } else {
                    if (contact.getResetFunction() == null) {
                        combCount++;
                        combLiteralCount += FormulaUtils.countLiterals(contact.getSetFunction());
                    } else {
                        seqCount++;
                        seqSetLiteralCount += FormulaUtils.countLiterals(contact.getSetFunction());
                        seqResetLiteralCount += FormulaUtils.countLiterals(contact.getResetFunction());
                    }
                }
            }
        }
        int driverCount = combCount + seqCount + undefinedCount;
        int seqLiteralCount = seqSetLiteralCount + seqResetLiteralCount;

        return "Circuit analysis:"
                + "\n  Component count (mapped + unmapped) -  " + components.size()
                + " (" + mappedCount + " + " + unmappedCount + ")"
                + "\n  Area -  " + gateArea + getNamedComponentArea(namedComponents)
                + (unmappedCount == 0 ? "" : " + " + unmappedCount + "*[unmapped]")
                + "\n  Non-trivial component count (function + blackbox) -  "
                + nontrivialCount + " (" + otherCount + " + " + blackboxCount + ")"
                + "\n  Trivial gate count (buffer / 0-delay + inverter / 0-delay + const) -  "
                + trivialCount + " (" + bufCount + " / " + bufZeroDelayCount + " + "
                + invCount + " / " + invZeroDelayCount + " + " + constCount + ")"
                + "\n  Driver pin count (combinational + sequential + undefined) -  "
                + driverCount + " (" + combCount + " + " + seqCount + " + " + undefinedCount + ")"
                + "\n  Literal count combinational / sequential (set + reset) -  "
                + combLiteralCount + " / " + seqLiteralCount + " (" + seqSetLiteralCount + " + " + seqResetLiteralCount + ")"
                + "\n  Port count (input + output) -  " + ports.size() + " (" + inPortCount + " + " + outPortCount + ")"
                + "\n  Max fanin / fanout -  " + getMaxValue(fanin) + " / " + getMaxValue(fanout)
                + "\n  Fanin distribution [0 / 1 / 2 ...] -  " + getDistribution(fanin)
                + "\n  Fanout distribution [0 / 1 / 2 ...] -  " + getDistribution(fanout)
                + "\n  Isolated components / ports / pins -  "
                + isolatedComponentCount + " / " + isolatedPortCount + " / " + isolatedPinCount
                + '\n';
    }

    private int getMaxValue(MultiSet<Integer> multiset) {
        int result = 0;
        for (Integer item : multiset.toSet()) {
            if (item > result) {
                result = item;
            }
        }
        return result;
    }

    private String getDistribution(MultiSet<Integer> multiset) {
        StringBuilder result = new StringBuilder();
        int max = getMaxValue(multiset);
        for (int i = 0; i <= Math.min(max, MAX_DISTRIBUTION); ++i) {
            if (!result.isEmpty()) {
                result.append(" / ");
            }
            result.append(multiset.count(i));
        }
        if (max > MAX_DISTRIBUTION) {
            result.append(" ...");
        }
        return result.toString();
    }

    private String getNamedComponentArea(MultiSet<String> multiset) {
        StringBuilder result = new StringBuilder();
        for (String item : multiset.toSet()) {
            int count = multiset.count(item);
            result.append(" + ").append(count > 1 ? count + "*" : "").append(item);
        }
        return result.toString();
    }

}
