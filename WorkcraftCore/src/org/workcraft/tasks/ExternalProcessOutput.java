package org.workcraft.tasks;

import org.workcraft.utils.TextUtils;

public class ExternalProcessOutput {

    private final int returnCode;
    private final byte[] stdout;
    private final byte[] stderr;

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
        return new String(stdout);
    }

    public byte[] getStderr() {
        return stderr;
    }

    public String getStderrString() {
        return new String(stderr);
    }

    public String getErrorsHeadAndTail() {
        return TextUtils.getHeadAndTail(getStderrString(), 10, 10);
    }

}
