package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    public static final String NET_FILE_NAME = "net.g";
    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    public static final String STG_FILE_NAME = "mpsat.g";
    public static final String COMP_FILE_NAME = "comp.xml";

    private final byte[] netInput;
    private final byte[] compInput;
    private final byte[] stgOutput;

    public MpsatOutput(ExternalProcessOutput output) {
        this(output, null, null, null);
    }

    @SuppressWarnings("serial")
    public MpsatOutput(ExternalProcessOutput output, byte[] netInput, byte[] compInput, byte[] stgOutput) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr(), new HashMap<String, byte[]>() {
            {
                put(NET_FILE_NAME, netInput);
                put(COMP_FILE_NAME, compInput);
                put(STG_FILE_NAME, stgOutput);
            }
        });
        this.netInput = netInput;
        this.compInput = compInput;
        this.stgOutput = stgOutput;
    }

    public byte[] getNetInput() {
        return netInput;
    }

    public byte[] getCompInput() {
        return compInput;
    }

    public byte[] getStgOutput() {
        return stgOutput;
    }

}
