package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.Not;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.visitors.StringGenerator.Style;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class VerilogSerialiser implements ModelSerialiser {

    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_WIRE = "wire";
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_ENDMODULE = "endmodule";
    private static final String KEYWORD_ASSIGN = "assign";
    private static final String KEYWORD_ASSIGN_DELAY = "#";

    private final Queue<Pair<File, Circuit>> refinementCircuits = new LinkedList<>();
    private final Map<String, SubstitutionRule> substitutionRules = new HashMap<>();

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (model instanceof Circuit) {
            Circuit circuit = (Circuit) model;
            Set<FunctionComponent> badComponents = RefinementUtils.getIncompatibleRefinementCircuitComponents(circuit);
            if (badComponents.isEmpty()) {
                PrintWriter writer = new PrintWriter(out);
                writer.println(Info.getGeneratedByText("// Verilog netlist ", ""));
                refinementCircuits.clear();
                substitutionRules.clear();
                substitutionRules.putAll(SubstitutionUtils.readExportSubstitutionRules());
                writeCircuit(writer, circuit);
                writeRefinementCircuits(writer);
                writer.close();
            } else {
                String msg = TextUtils.wrapMessageWithItems("Incompatible refinement interface for component",
                        ReferenceHelper.getReferenceSet(circuit, badComponents));

                DialogUtils.showError(msg);
            }
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        return refs;
    }

    private void writeRefinementCircuits(PrintWriter writer) {
        Set<String> visited = new HashSet<>();
        while (!refinementCircuits.isEmpty()) {
            Pair<File, Circuit> refinement = refinementCircuits.remove();
            String path = FileUtils.getFullPath(refinement.getFirst());
            if (!visited.contains(path)) {
                visited.add(path);
                Circuit circuit = refinement.getSecond();
                writer.println();
                writeCircuit(writer, circuit);
            }
        }
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public UUID getFormatUUID() {
        return VerilogFormat.getInstance().getUuid();
    }

    private void writeCircuit(PrintWriter writer, Circuit circuit) {
        CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit);
        writeHeader(writer, circuitInfo);
        writeInstances(writer, circuitInfo);
        writeInitialState(writer, circuitInfo);
        writer.println(KEYWORD_ENDMODULE);
    }

    private void writeHeader(PrintWriter writer, CircuitSignalInfo circuitInfo) {
        String title = ExportUtils.asIdentifier(circuitInfo.getCircuit().getTitle());
        writer.print(KEYWORD_MODULE + " " + title + " (");
        Set<String> inputPorts = new LinkedHashSet<>();
        Set<String> outputPorts = new LinkedHashSet<>();
        boolean isFirstPort = true;
        for (Contact contact : circuitInfo.getCircuit().getPorts()) {
            if (isFirstPort) {
                isFirstPort = false;
            } else {
                writer.print(", ");
            }
            String signal = circuitInfo.getContactSignal(contact);
            writer.print(signal);
            if (contact.isInput()) {
                inputPorts.add(signal);
            } else {
                outputPorts.add(signal);
            }
        }
        writer.println(");");
        if (!inputPorts.isEmpty()) {
            writer.println("    " + KEYWORD_INPUT + " " + String.join(", ", inputPorts) + ";");
        }
        if (!outputPorts.isEmpty()) {
            writer.println("    " + KEYWORD_OUTPUT + " " + String.join(", ", outputPorts) + ";");
        }
        Set<String> wires = new LinkedHashSet<>();
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                String signal = circuitInfo.getContactSignal(contact);
                if (inputPorts.contains(signal) || outputPorts.contains(signal)) continue;
                wires.add(signal);
            }
        }
        if (!wires.isEmpty()) {
            writer.println("    " + KEYWORD_WIRE + " " + String.join(", ", wires) + ";");
        }
        writer.println();
    }

    private void writeInstances(PrintWriter writer, CircuitSignalInfo circuitInfo) {
        // Write writer assign statements
        boolean hasAssignments = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (!component.isMapped() && (component.getRefinementFile() == null)) {
                if (writeAssigns(writer, circuitInfo, component)) {
                    hasAssignments = true;
                } else {
                    String ref = circuitInfo.getComponentReference(component);
                    LogUtils.logError("Unmapped component '" + ref + "' cannot be exported as assign statements.");
                }
            }
        }
        if (hasAssignments) {
            writer.println();
        }
        // Write writer mapped components
        boolean hasMappedComponents = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (component.isMapped() || (component.getRefinementFile() != null)) {
                writeInstance(writer, circuitInfo, component);
                hasMappedComponents = true;
            }
        }
        if (hasMappedComponents) {
            writer.println();
        }
    }

    private boolean writeAssigns(PrintWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        LogUtils.logWarning("Component '" + instanceFlatName + "' is not associated to a module and is exported as assign statement.");
        for (CircuitSignalInfo.SignalInfo signalInfo: circuitInfo.getComponentSignalInfos(component)) {
            String signalName = circuitInfo.getContactSignal(signalInfo.contact);
            BooleanFormula setFormula = signalInfo.setFormula;
            String setExpr = StringGenerator.toString(setFormula, Style.VERILOG);
            BooleanFormula resetFormula = signalInfo.resetFormula;
            if (resetFormula != null) {
                resetFormula = new Not(resetFormula);
            }
            String resetExpr = StringGenerator.toString(resetFormula, Style.VERILOG);
            String expr = null;
            if (!setExpr.isEmpty() && !resetExpr.isEmpty()) {
                expr = setExpr + " | " + signalName + " & (" + resetExpr + ")";
            } else if (!setExpr.isEmpty()) {
                expr = setExpr;
            } else if (!resetExpr.isEmpty()) {
                expr = resetExpr;
            }
            if (expr != null) {
                writer.println("    " + KEYWORD_ASSIGN + getDelayParameter() + " " + signalName + " = " + expr + ";");
                result = true;
            }
        }
        return result;
    }

    private String getDelayParameter() {
        int assignDelay = CircuitSettings.getVerilogAssignDelay();
        return assignDelay > 0 ? " " + KEYWORD_ASSIGN_DELAY + assignDelay : "";
    }

    private void writeInstance(PrintWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        // Module name
        String title = component.getModule();
        Pair<File, Circuit> refinementCircuit = RefinementUtils.getRefinementCircuit(component);
        if (refinementCircuit != null) {
            refinementCircuits.add(refinementCircuit);
            title = refinementCircuit.getSecond().getTitle();
        }
        String moduleName = ExportUtils.asIdentifier(title);
        // Instance name
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        String msg = "Processing instance '" + instanceFlatName + "'";
        String circuitTitle = circuitInfo.getCircuit().getTitle();
        if (!circuitTitle.isEmpty()) {
            msg += " in circuit '" + circuitTitle + "'";
        }
        msg += ": ";
        moduleName = SubstitutionUtils.getModuleSubstitutionName(moduleName, substitutionRule, msg);
        if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
            writer.println("    // This inverter should have a short delay");
        }
        writer.print("    " + moduleName + " " + instanceFlatName + " (");
        boolean first = true;
        for (Contact contact: component.getContacts()) {
            if (first) {
                first = false;
            } else {
                writer.print(", ");
            }
            String signalName = circuitInfo.getContactSignal(contact);
            if ((signalName == null) || signalName.isEmpty()) {
                String contactName = contact.getName();
                LogUtils.logWarning("In component '" + instanceFlatName + "' contact '" + contactName + "' is disconnected.");
                signalName = "";
            }
            String contactName = SubstitutionUtils.getContactSubstitutionName(contact.getName(), substitutionRule, msg);
            writer.print("." + contactName + "(" + signalName + ")");
        }
        writer.println(");");
    }

    private void writeInitialState(PrintWriter writer, CircuitSignalInfo circuitInfo) {
        Collection<Contact> drivers = circuitInfo.getCircuit().getDrivers();
        if (!drivers.isEmpty()) {
            writer.println("    // signal values at the initial state:");
            writer.print("    //");
            for (Contact driver: drivers) {
                String signalName = circuitInfo.getContactSignal(driver);
                if ((signalName != null) && !signalName.isEmpty()) {
                    writer.print(" ");
                    if (!driver.getInitToOne()) {
                        writer.print("!");
                    }
                    writer.print(signalName);
                }
            }
            writer.println();
        }
    }

}
