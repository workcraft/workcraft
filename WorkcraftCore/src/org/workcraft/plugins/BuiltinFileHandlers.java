package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.workspace.handlers.SystemOpen;
import org.workcraft.plugins.workspace.handlers.WorkcraftOpen;
import org.workcraft.workspace.FileHandler;

public class BuiltinFileHandlers implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(FileHandler.class, new Initialiser<FileHandler>() {
            @Override
            public FileHandler create() {
                return new WorkcraftOpen();
                }
            });

        pm.registerClass(FileHandler.class, SystemOpen.class);
    }

    @Override
    public String getDescription() {
        return "Built-in file operations for Workspace";
    }
}
