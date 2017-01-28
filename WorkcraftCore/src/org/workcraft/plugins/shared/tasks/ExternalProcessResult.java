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

    public String getErrorsHeadAndTail() {
        return getErrorsHeadAndTail(10, 10);
    }

    public String getErrorsHeadAndTail(int firstCount, int lastCount) {
        return getHeadAndTail(new String(getErrors()), firstCount, lastCount);
    }

    private String getHeadAndTail(String text, int firstCount, int lastCount) {
        String result = "";
        String[] lines = text.split("\n");
        int index = 0;
        boolean dotsInserted = false;
        for (String line: lines) {
            if ((index < firstCount) || (index >= lines.length - lastCount)) {
                result += line + "\n";
            } else if (!dotsInserted) {
                result += "...\n";
                dotsInserted = true;
            }
            index++;
        }
        return result;
    }

}