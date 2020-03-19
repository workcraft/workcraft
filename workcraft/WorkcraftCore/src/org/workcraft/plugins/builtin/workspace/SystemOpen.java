package org.workcraft.plugins.builtin.workspace;

import org.workcraft.utils.DesktopApi;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.FileHandler;

import java.io.File;

public class SystemOpen implements FileHandler {

    @Override
    public boolean accept(File file) {
        return !FileFilters.isWorkFile(file);
    }

    @Override
    public void execute(File file) {
        DesktopApi.open(file);
    }

    @Override
    public String getDisplayName() {
        return "Open using system default program";
    }

}
