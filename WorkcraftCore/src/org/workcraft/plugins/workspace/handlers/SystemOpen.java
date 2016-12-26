package org.workcraft.plugins.workspace.handlers;

import java.io.File;

import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.FileFilters;
import org.workcraft.workspace.FileHandler;

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
