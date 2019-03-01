package org.workcraft.plugins.builtin.workspace;

import org.workcraft.utils.DesktopApi;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.FileHandler;

import java.io.File;

public class SystemOpen implements FileHandler {

    @Override
    public boolean accept(File f) {
        return !f.getName().endsWith(FileFilters.DOCUMENT_EXTENSION);
    }

    @Override
    public void execute(File f) {
        DesktopApi.open(f);
    }

    @Override
    public String getDisplayName() {
        return "Open using system default program";
    }

}
