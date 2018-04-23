package org.workcraft.plugins.circuit.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.DumbBooleanWorker;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.formula.utils.FormulaToString.Style;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSignalInfo;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.LogUtils;

public class VerilogSerialiser implements ModelSerialiser {

    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_ENDMODULE = "endmodule";
    private static final String KEYWORD_ASSIGN = "assign";

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (model instanceof Circuit) {
            PrintWriter writer = new PrintWriter(out);
            writer.write(Info.getGeneratedByText("// Verilog netlist ", "\n"));
            writeCircuit(writer, (Circuit) model);
            writer.close();
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        return refs;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public UUID getFormatUUID() {
        return VerilogFormat.getInstance().getUuid();
    }

    private void writeCircuit(PrintWriter out, Circuit circuit) {
        CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit);
        writeHeader(out, circuit);
        writeInstances(out, circuitInfo);
        writeInitialState(out, circuitInfo);
        out.println(KEYWORD_ENDMODULE);
    }

    private void writeHeader(PrintWriter out, Circuit circuit) {
        String topName = circuit.getTitle();
        if ((topName == null) || topName.isEmpty()) {
            topName = "UNTITLED";
            LogUtils.logWarning("The top module does not have a name. Exporting as '" + topName + "' module.");
        }
        out.print(KEYWORD_MODULE + " " + topName + " (");
        String inputPorts = "";
        String outputPorts = "";
        boolean isFirstPort = true;
        for (Contact contact: circuit.getPorts()) {
            if (isFirstPort) {
                isFirstPort = false;
            } else {
                out.print(", ");
            }
            String contactRef = circuit.getNodeReference(contact);
            String contactFlatName = NamespaceHelper.flattenReference(contactRef);
            out.print(contactFlatName);
            if (contact.isInput()) {
                if (!inputPorts.isEmpty()) {
                    inputPorts += ", ";
                }
                inputPorts += contactFlatName;
            } else {
                if (!outputPorts.isEmpty()) {
                    outputPorts += ", ";
                }
                outputPorts += contactFlatName;
            }
        }
        out.println(");");
        if (!inputPorts.isEmpty()) {
            out.println("    " + KEYWORD_INPUT + " " + inputPorts + ";");
        }
        if (!outputPorts.isEmpty()) {
            out.println("    " + KEYWORD_OUTPUT + " " + outputPorts + ";");
        }
        out.println();
    }

    private void writeInstances(PrintWriter out, CircuitSignalInfo circuitInfo) {
        HashMap<String, SubstitutionRule> substitutionRules = SubstitutionUtils.readSubsritutionRules();
        Collection<FunctionComponent> functionComponents = circuitInfo.getCircuit().getFunctionComponents();
        // Write out assign statements
        boolean hasAssignments = false;
        for (FunctionComponent component: functionComponents) {
            if (!component.isMapped()) {
                if (writeAssigns(out, circuitInfo, component)) {
                    hasAssignments = true;
                } else {
                    String ref = circuitInfo.getComponentReference(component);
                    LogUtils.logError("Unmapped component '" + ref + "' cannot be exported as assign statements.");
                }
            }
        }
        if (hasAssignments) {
            out.print("\n");
        }
        // Write out mapped components
        for (FunctionComponent component: functionComponents) {
            if (component.isMapped()) {
                writeInstance(out, circuitInfo, component, substitutionRules);
            }
        }
    }

    private boolean writeAssigns(PrintWriter out, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        LogUtils.logWarning("Component '" + instanceFlatName + "' is not associated to a module and is exported as assign statements.");
        for (CircuitSignalInfo.SignalInfo signalInfo: circuitInfo.getComponentSignalInfos(component)) {
            String signalName = circuitInfo.getContactSignal(signalInfo.contact);
            BooleanFormula setFormula = signalInfo.setFormula;
            String setExpr = FormulaToString.toString(setFormula, Style.VERILOG);
            BooleanFormula resetFormula = signalInfo.resetFormula;
            if (resetFormula != null) {
                resetFormula = new DumbBooleanWorker().not(resetFormula);
            }
            String resetExpr = FormulaToString.toString(resetFormula, Style.VERILOG);
            String expr = null;
            if (!setExpr.isEmpty() && !resetExpr.isEmpty()) {
                expr = setExpr + " | " + signalName + " & (" + resetExpr + ")";
            } else if (!setExpr.isEmpty()) {
                expr = setExpr;
            } else if (!resetExpr.isEmpty()) {
                expr = resetExpr;
            }
            if ((expr != null) && !expr.isEmpty()) {
                out.println("    " + KEYWORD_ASSIGN + " " + signalName + " = " + expr + ";");
                result = true;
            }
        }
        return result;
    }

    private void writeInstance(PrintWriter out, CircuitSignalInfo circuitInfo, FunctionComponent component,
            HashMap<String, SubstitutionRule> substitutionRules) {
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        String moduleName = component.getModule();
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        if (substitutionRule != null) {
            String newModuleName = substitutionRule.newName;
            if (newModuleName != null) {
                LogUtils.logInfo("In component '" + instanceFlatName + "' renaming module '" + moduleName + "' to '" + newModuleName + "'.");
                moduleName = newModuleName;
            }
        }
        if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
            out.println("    // This inverter should have a short delay");
        }
        out.print("    " + moduleName + " " + instanceFlatName + " (");
        boolean first = true;
        for (Contact contact: component.getContacts()) {
            if (first) {
                first = false;
            } else {
                out.print(", ");
            }
            String signalName = circuitInfo.getContactSignal(contact);
            if ((signalName == null) || signalName.isEmpty()) {
                String contactName = contact.getName();
                LogUtils.logWarning("In component '" + instanceFlatName + "' contact '" + contactName + "' is disconnected.");
                signalName = "";
            }
            String contactName = SubstitutionUtils.getContactSubstitutionName(contact, substitutionRule, instanceFlatName);
            out.print("." + contactName + "(" + signalName + ")");
        }
        out.print(");\n");
    }

    private void writeInitialState(PrintWriter out, CircuitSignalInfo circuitInfo) {
        Collection<Contact> drivers = circuitInfo.getCircuit().getDrivers();
        if (!drivers.isEmpty()) {
            out.println();
            out.println("    // signal values at the initial state:");
            out.print("    //");
            for (Contact contact: drivers) {
                String signalName = circuitInfo.getContactSignal(contact);
                if ((signalName != null) && !signalName.isEmpty()) {
                    out.print(" ");
                    if (!contact.getInitToOne()) {
                        out.print("!");
                    }
                    out.print(signalName);
                }
            }
            out.println();
        }
    }

}
