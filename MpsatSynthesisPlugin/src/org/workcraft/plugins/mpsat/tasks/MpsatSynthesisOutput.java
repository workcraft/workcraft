package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatSynthesisOutput extends ExternalProcessOutput {

    public static final String VERILOG_FILE_NAME = "mpsat.v";
    public static final String STG_FILE_NAME = "mpsat.g";

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
