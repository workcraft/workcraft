/*
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All rights reserved.
 */

package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.Info;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.Identifier;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.interop.SdcFormat;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

public class PathbreakConstraintSerialiser implements PathbreakSerialiser {

    private static final String KEYWORD_SET_DISABLE_TIMING = "set_disable_timing";
    private static final String KEYWORD_FROM = "-from";
    private static final String KEYWORD_TO = "-to";

    @Override
    public void serialise(Circuit circuit, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        writer.write(Info.getGeneratedByText("# Path break SDC file ", "\n"));
        writeCircuit(writer, circuit);
        writer.close();
    }

    @Override
    public UUID getFormatUUID() {
        return SdcFormat.getInstance().getUuid();
    }

    @Override
    public String getName() {
        return "Path breaker SDC constraints";
    }

    private void writeCircuit(PrintWriter out, Circuit circuit) {
        for (FunctionComponent component: Hierarchy.getDescendantsOfType(circuit.getRoot(), FunctionComponent.class)) {
            writeInstance(out, circuit, component);
        }
    }

    private void writeInstance(PrintWriter out, Circuit circuit, FunctionComponent component) {

        String instanceRef = Identifier.truncateNamespaceSeparator(circuit.getNodeReference(component));
        String instanceFlatName = NamespaceHelper.flattenReference(instanceRef);
        String msg = "Processing instance '" + instanceFlatName + "': ";
        if (!component.isMapped()) {
            LogUtils.logWarning("Disabling timing arc in unmapped component '" + instanceRef + "'");
        }
        String moduleName = component.getModule();
        Map<String, SubstitutionRule> substitutionRules = LibraryManager.getExportSubstitutionRules();
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        for (Contact outputContact: component.getOutputs()) {
            String outputName = null;
            for (Contact inputContact: component.getInputs()) {
                if (inputContact.getPathBreaker()) {
                    if (outputName == null) {
                        outputName = SubstitutionUtils.getContactSubstitutionName(
                                outputContact.getName(), substitutionRule, msg);
                    }
                    String inputName = SubstitutionUtils.getContactSubstitutionName(
                            inputContact.getName(), substitutionRule, msg);

                    out.write(KEYWORD_SET_DISABLE_TIMING + " " + instanceFlatName + " " +
                            KEYWORD_FROM + " " + inputName + " " + KEYWORD_TO + " " + outputName + "\n");
                }
            }
        }
    }

}
