package org.workcraft.plugins.circuit.verilog;

import org.workcraft.plugins.circuit.CircuitSettings;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class VerilogWriter extends PrintWriter {

    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_WIRE = "wire";
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_ENDMODULE = "endmodule";
    private static final String KEYWORD_ASSIGN = "assign";
    private static final String KEYWORD_ASSIGN_DELAY = "#";
    private static final String KEYWORD_TIMESCALE = "`timescale";
    private static final String KEYWORD_TIMEUNIT = "timeunit";

    public VerilogWriter(OutputStream out) {
        super(out);
    }

    public enum SignalType {
        INPUT(KEYWORD_INPUT),
        OUTPUT(KEYWORD_OUTPUT),
        WIRE(KEYWORD_WIRE);

        public final String name;

        SignalType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public void writeModuleIntro(String title,
            Collection<String> inputPorts, Collection<VerilogBus> inputBuses,
            Collection<String> outputPorts, Collection<VerilogBus> outputBuses) {

        LinkedHashSet<String> ports = new LinkedHashSet<>();
        ports.addAll(inputPorts);
        if (inputBuses != null) {
            ports.addAll(inputBuses.stream().map(VerilogBus::getName).toList());
        }
        ports.addAll(outputPorts);
        if (outputBuses != null) {
            ports.addAll(outputBuses.stream().map(VerilogBus::getName).toList());
        }

        write(KEYWORD_MODULE + ' ' + title + " (");
        boolean isFirstPort = true;
        for (String port : ports) {
            if (isFirstPort) {
                isFirstPort = false;
            } else {
                write(", ");
            }
            write(port);
        }
        write(");\n");
    }

    public void writeSignalDefinitions(SignalType signalType, Collection<String> signals, Collection<VerilogBus> buses) {
        if (!signals.isEmpty()) {
            write("    " + signalType + ' ' + String.join(", ", signals) + ";\n");
        }
        for (VerilogBus bus : buses) {
            Integer maxIndex = bus.getMaxIndex();
            Integer minIndex = bus.getMinIndex();
            String name = bus.getName();
            write("    " + signalType + " [" + maxIndex + ':' + minIndex + "] " + name + ";\n");
        }
    }

    public void writeTimescaleDefinition() {
        String timescale = CircuitSettings.getVerilogTimescale();
        if ((timescale != null) && !timescale.isEmpty()) {
            write(KEYWORD_TIMESCALE + ' ' + timescale + "\n\n");
        }
    }

    public void writeTimeunitDefinition() {
        String timescale = CircuitSettings.getVerilogTimescale();
        if ((timescale != null) && !timescale.isEmpty()) {
            write("    " + KEYWORD_TIMEUNIT + ' ' + timescale + ";\n\n");
        }
    }

    public void writeAssign(Supplier<String> delaySupplier, String signal, String expr) {
        String delay = (delaySupplier == null) ? null : delaySupplier.get();
        String assignDelay = (delay == null) || delay.isEmpty() || "0".equals(delay.trim())
                ? "" : ' ' + KEYWORD_ASSIGN_DELAY + delay;

        write("    " +  KEYWORD_ASSIGN + assignDelay + ' ' + signal + " = ");
        if (expr != null) {
            write(expr + ";\n");
        }
    }

    public void writeModuleOutro() {
        write(KEYWORD_ENDMODULE + '\n');
    }

}
