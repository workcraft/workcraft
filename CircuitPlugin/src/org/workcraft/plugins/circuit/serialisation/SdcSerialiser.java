package org.workcraft.plugins.circuit.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.UUID;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;

public class SdcSerialiser implements ModelSerialiser {

    private static final String KEYWORD_SET_DISABLE_TIMING = "set_disable_timing";
    private static final String KEYWORD_FROM = "-from";
    private static final String KEYWORD_TO = "-to";

    class ReferenceResolver implements ReferenceProducer {
        HashMap<Object, String> refMap = new HashMap<>();

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (model instanceof Circuit) {
            String instancePrefix = DialogUtils.showInput("Prefix to add to all instance names:", "");
            if (instancePrefix != null) {
                PrintWriter writer = new PrintWriter(out);
                writer.write(Info.getGeneratedByText("// SDC file ", "\n"));
                writeCircuit(writer, (Circuit) model, instancePrefix);
                writer.close();
            }
            return new ReferenceResolver();
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public String getDescription() {
        return "Workcraft SDC serialiser";
    }

    @Override
    public String getExtension() {
        return ".sdc";
    }

    @Override
    public UUID getFormatUUID() {
        return Format.SDC;
    }

    private void writeCircuit(PrintWriter out, Circuit circuit, String instancPrefix) {
        HashMap<String, SubstitutionRule> substitutionRules = SubstitutionUtils.readSubsritutionRules();
        // Write out mapped components
        for (FunctionComponent component: Hierarchy.getDescendantsOfType(circuit.getRoot(), FunctionComponent.class)) {
            if (component.isMapped()) {
                writeInstance(out, circuit, instancPrefix, component, substitutionRules);
            }
        }
    }

    private void writeInstance(PrintWriter out, Circuit circuit, String instancePrefix, FunctionComponent component,
            HashMap<String, SubstitutionRule> substitutionRules) {

        String instanceRef = circuit.getNodeReference(component);
        String instanceFlatName = NamespaceHelper.flattenReference(instanceRef);
        if ((instancePrefix != null) && !instancePrefix.isEmpty()) {
            instanceFlatName = instancePrefix + instanceFlatName;
        }
        String moduleName = component.getModule();
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        for (Contact outputContact: component.getOutputs()) {
            String outputName = null;
            for (Contact inputContact: component.getInputs()) {
                if (inputContact.getPathBreaker()) {
                    if (outputName == null) {
                        outputName = SubstitutionUtils.getContactSubstitutionName(outputContact, substitutionRule, instanceFlatName);
                    }
                    String inputName = SubstitutionUtils.getContactSubstitutionName(inputContact, substitutionRule, instanceFlatName);
                    out.write(KEYWORD_SET_DISABLE_TIMING + " " + instanceFlatName + " " +
                            KEYWORD_FROM + " " + inputName + " " + KEYWORD_TO + " " + outputName + "\n");
                }
            }
        }
    }

}
