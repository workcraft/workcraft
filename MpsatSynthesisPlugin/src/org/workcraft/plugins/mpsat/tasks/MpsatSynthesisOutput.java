package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatSynthesisOutput extends ExternalProcessOutput {

    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    public static final String STG_FILE_NAME = "mpsat.g";
    public static final String VERILOG_FILE_NAME = "mpsat.v";

    private final byte[] stgOutput;
    private final byte[] verilogOutput;

    public MpsatSynthesisOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    @SuppressWarnings("serial")
    public MpsatSynthesisOutput(ExternalProcessOutput output, byte[] stgOutput, byte[] verilogOutput) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr(), new HashMap<String, byte[]>() {
            {
                put(STG_FILE_NAME, stgOutput);
                put(VERILOG_FILE_NAME, verilogOutput);
            }
        });
        this.stgOutput = stgOutput;
        this.verilogOutput = verilogOutput;
    }

    public byte[] getStgOutptu() {
        return stgOutput;
    }

    public byte[] getVerilog() {
        return verilogOutput;
    }

}
