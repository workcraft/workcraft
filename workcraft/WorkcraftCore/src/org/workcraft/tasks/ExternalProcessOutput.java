package org.workcraft.tasks;

import org.workcraft.utils.TextUtils;

import java.nio.charset.StandardCharsets;

public class ExternalProcessOutput {

    private final int returnCode;
    private final byte[] stdout;
    private final byte[] stderr;

    public ExternalProcessOutput(int returnCode) {
        this(returnCode, new byte[0], new byte[0]);
    }

    public ExternalProcessOutput(int returnCode, byte[] stdout, byte[] stderr) {
        this.returnCode = returnCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public byte[] getStdout() {
        return stdout;
    }

    public String getStdoutString() {
        return new String(stdout, StandardCharsets.UTF_8);
    }

    public byte[] getStderr() {
        return stderr;
    }

    public String getStderrString() {
        return new String(stderr, StandardCharsets.UTF_8);
    }

    public String getErrorsHeadAndTail() {
        return TextUtils.getHeadAndTail(getStderrString(), 10, 10);
    }

}
