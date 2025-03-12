package org.workcraft.plugins.dfs.interop;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.ExportUtils;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VerilogExporter implements Exporter {

    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_ENDMODULE = "endmodule";

    private static final String SEPARATOR = "_";
    private static final String NAME_OUT = "OUT";
    private static final String NAME_IN = "IN";
    private static final String NAME_RI = "RI";
    private static final String NAME_AI = "AI";
    private static final String NAME_RO = "RO";
    private static final String NAME_AO = "AO";
    private static final String NAME_BUFFER = "BUFFER";
    private static final String PREFIX_CELEMENT = "CELEMENT" + SEPARATOR;
    private static final String PREFIX_INST = "INST_";
    private static final String PREFIX_WIRE = "WIRE_";
    private static final String PREFIX_OUT = NAME_OUT + SEPARATOR;
    private static final String PREFIX_IN = NAME_IN + SEPARATOR;
    private static final String PREFIX_RI = NAME_RI + SEPARATOR;
    private static final String PREFIX_AI = NAME_AI + SEPARATOR;
    private static final String PREFIX_RO = NAME_RO + SEPARATOR;
    private static final String PREFIX_AO = NAME_AO + SEPARATOR;

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof Dfs;
    }

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public void serialise(Model model, OutputStream out) {
        if (model instanceof Dfs) {
            PrintWriter writer = new PrintWriter(out);
            String moduleName = ExportUtils.getTitleAsIdentifier(model.getTitle());
            File file = getCurrentFile();
            VerilogFormat format = getFormat();
            writer.write(ExportUtils.getExportHeader("Verilog netlist", "//", moduleName, file, format));
            writeModule(writer, (Dfs) model, moduleName);
            writer.close();
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
    }

    private void writeModule(PrintWriter out, Dfs dfs, String moduleName) {
        writeHeader(out, dfs, moduleName);
        writeInstances(out, dfs);
        out.println(KEYWORD_ENDMODULE);
    }

    private void writeHeader(PrintWriter out, Dfs dfs, String moduleName) {
        ArrayList<Pair<String, Boolean>> ports = new ArrayList<>();
        for (MathNode node: dfs.getAllNodes()) {
            String ref = dfs.getNodeReference(node);
            if (dfs.getPreset(node).isEmpty()) {
                ports.add(new Pair<>(PREFIX_IN + ref, false));
            }
            if (dfs.getPostset(node).isEmpty()) {
                ports.add(new Pair<>(PREFIX_OUT + ref, true));
            }
        }
        for (MathNode node: dfs.getAllRegisters()) {
            String ref = dfs.getNodeReference(node);
            if (dfs.getRPreset(node).isEmpty()) {
                ports.add(new Pair<>(PREFIX_RI + ref, false));
                ports.add(new Pair<>(PREFIX_AI + ref, true));
            }
            if (dfs.getRPostset(node).isEmpty()) {
                ports.add(new Pair<>(PREFIX_RO + ref, true));
                ports.add(new Pair<>(PREFIX_AO + ref, false));
            }
        }
        out.println(KEYWORD_MODULE + ' ' + moduleName + " (");
        boolean isFirstPort = true;
        for (Pair<String, Boolean> port: ports) {
            if (!isFirstPort) {
                out.print(',');
            }
            out.println();
            out.print("    " + port.getFirst());
            isFirstPort = false;
        }
        out.println(" );");
        out.println();
        for (Pair<String, Boolean> port: ports) {
            out.print("    ");
            if (port.getSecond()) {
                out.print(KEYWORD_OUTPUT);
            } else {
                out.print(KEYWORD_INPUT);
            }
            out.println(' ' + port.getFirst() + ';');
        }
        out.println();
    }

    private void writeInstances(PrintWriter out, Dfs dfs) {
        HashSet<MathNode> logicNodes = new HashSet<>();
        logicNodes.addAll(dfs.getLogics());
        logicNodes.addAll(dfs.getCounterflowLogics());
        for (MathNode node: logicNodes) {
            writeInstance(out, dfs, node);
        }
        for (MathNode node: dfs.getAllRegisters()) {
            writeInstance(out, dfs, node);
            writeCelementPred(out, dfs, node);
            writeCelementSucc(out, dfs, node);
        }
    }

    private void writeInstance(PrintWriter out, Dfs dfs, MathNode node) {
        Set<MathNode> preset = dfs.getPreset(node);
        Set<MathNode> postset = dfs.getPostset(node);
        String ref = dfs.getNodeReference(node);
        String instanceName = PREFIX_INST + ref;
        String className = node.getClass().getSimpleName().toUpperCase(Locale.ROOT);
        int inCount = preset.isEmpty() ? 1 : preset.size();
        int outCount = postset.isEmpty() ? 1 : postset.size();
        String moduleName = className + SEPARATOR + inCount + SEPARATOR + outCount;
        out.print("    " + moduleName + ' ' + instanceName + " (");
        boolean isFirstContact = true;
        int inIndex = 0;
        if (preset.isEmpty()) {
            String inWireName = PREFIX_IN + ref;
            String inContactName = NAME_IN + inIndex++;
            writeContact(out, inContactName, inWireName, isFirstContact);
            isFirstContact = false;
        }
        for (MathNode predNode: preset) {
            String predRef = dfs.getNodeReference(predNode);
            String inWireName = PREFIX_WIRE + predRef + SEPARATOR + ref;
            String inContactName = NAME_IN + inIndex++;
            writeContact(out, inContactName, inWireName, isFirstContact);
            isFirstContact = false;
        }
        int outIndex = 0;
        if (postset.isEmpty()) {
            String outWireName = PREFIX_OUT + ref;
            String outContactName = NAME_OUT + outIndex++;
            writeContact(out, outContactName, outWireName, isFirstContact);
            isFirstContact = false;
        }
        for (MathNode succNode: postset) {
            String succRef = dfs.getNodeReference(succNode);
            String outWireName = PREFIX_WIRE + ref + SEPARATOR + succRef;
            String outContactName = NAME_OUT + outIndex++;
            writeContact(out, outContactName, outWireName, isFirstContact);
            isFirstContact = false;
        }
        if ((node instanceof Register)
                || (node instanceof CounterflowRegister)
                || (node instanceof ControlRegister)
                || (node instanceof PushRegister)
                || (node instanceof PopRegister)) {
            writeContact(out, NAME_RI, PREFIX_WIRE + PREFIX_RI + ref, isFirstContact);
            writeContact(out, NAME_AI, PREFIX_WIRE + PREFIX_AI + ref, isFirstContact);
            writeContact(out, NAME_RO, PREFIX_WIRE + PREFIX_RO + ref, isFirstContact);
            writeContact(out, NAME_AO, PREFIX_WIRE + PREFIX_AO + ref, isFirstContact);
            isFirstContact = false;
        }
        out.println(");");
    }

    private void writeContact(PrintWriter out, String contactName, String wireName, boolean isFirstContact) {
        if (!isFirstContact) {
            out.print(", ");
        }
        out.print("." + contactName + "(" + wireName + ")");
    }

    private void writeCelementPred(PrintWriter out, Dfs dfs, MathNode node) {
        ArrayList<String> inWireNames = new ArrayList<>();
        Set<MathNode> rPreset = dfs.getRPreset(node);
        if (rPreset.isEmpty()) {
            String ref = dfs.getNodeReference(node);
            String inWireName = PREFIX_RI + ref;
            inWireNames.add(inWireName);
        }
        for (MathNode predNode: rPreset) {
            String predRef = dfs.getNodeReference(predNode);
            String inWireName = PREFIX_WIRE + PREFIX_RO + predRef;
            inWireNames.add(inWireName);
        }
        String ref = dfs.getNodeReference(node);
        String instanceName = PREFIX_INST + PREFIX_RI + ref;
        String outWireName = PREFIX_WIRE + PREFIX_RI + ref;
        writeCelement(out, instanceName, inWireNames, outWireName);
    }

    private void writeCelementSucc(PrintWriter out, Dfs dfs, MathNode node) {
        ArrayList<String> inWireNames = new ArrayList<>();
        Set<MathNode> rPostset = dfs.getRPostset(node);
        if (rPostset.isEmpty()) {
            String ref = dfs.getNodeReference(node);
            String inWireName = PREFIX_AO + ref;
            inWireNames.add(inWireName);
        }
        for (MathNode succNode: rPostset) {
            String succRef = dfs.getNodeReference(succNode);
            String inWireName = PREFIX_WIRE + PREFIX_AI + succRef;
            inWireNames.add(inWireName);
        }
        String ref = dfs.getNodeReference(node);
        String instanceName = PREFIX_INST + PREFIX_AO + ref;
        String outWireName = PREFIX_WIRE + PREFIX_AO + ref;
        writeCelement(out, instanceName, inWireNames, outWireName);
    }

    private void writeCelement(PrintWriter out, String instanceName, ArrayList<String> inWireNames, String outWireName) {
        int inCount = inWireNames.size();
        if (inCount == 1) {
            out.print("    " + NAME_BUFFER + ' ' + instanceName + " (");
            String inWireName = inWireNames.get(0);
            writeContact(out, NAME_IN, inWireName, true);
            writeContact(out, NAME_OUT, outWireName, false);
            out.println(");");
        } else if (inCount > 1) {
            String moduleName = PREFIX_CELEMENT + inCount;
            out.print("    " + moduleName + ' ' + instanceName + " (");
            boolean isFirstContact = true;
            int inIndex = 0;
            for (String inWireName: inWireNames) {
                String inContactName = NAME_IN + inIndex++;
                writeContact(out, inContactName, inWireName, isFirstContact);
                isFirstContact = false;
            }
            writeContact(out, NAME_OUT, outWireName, isFirstContact);
            out.println(");");
        }
    }

}
