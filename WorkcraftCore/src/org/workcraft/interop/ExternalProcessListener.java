package org.workcraft.interop;

public interface ExternalProcessListener {
    void outputData(byte[] data);
    void errorData(byte[] data);
    void processFinished(int returnCode);
}
