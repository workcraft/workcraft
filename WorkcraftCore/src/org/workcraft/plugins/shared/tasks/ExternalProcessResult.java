package org.workcraft.plugins.shared.tasks;

import java.util.HashMap;
import java.util.Map;

public class ExternalProcessResult {
    private final byte[] output;
    private final byte[] errors;
    private final int returnCode;
    private final Map<String, byte[]> fileDataMap;

    public ExternalProcessResult(int returnCode, byte[] output, byte[] errors) {
        this(returnCode, output, errors, new HashMap<String, byte[]>());
    }

    public ExternalProcessResult(int returnCode, byte[] output, byte[] errors, Map<String, byte[]> fileDataMap) {
        this.output = output;
        this.errors = errors;
        this.returnCode = returnCode;
        this.fileDataMap = fileDataMap;
    }

    public byte[] getOutput() {
        return output;
    }

    public byte[] getErrors() {
        return errors;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public byte[] getFileData(String name) {
        return fileDataMap.get(name);
    }
}