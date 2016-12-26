package org.workcraft.workspace;

import java.io.File;

public interface FileHandler {
    boolean accept(File f);
    void execute(File f);
    String getDisplayName();
}
