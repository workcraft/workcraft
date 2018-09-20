package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.interop.*;

public class BuiltinExporters implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerExporter(DotExporter.class);
        pm.registerExporter(SvgExporter.class);
        pm.registerExporter(PdfExporter.class);
        pm.registerExporter(PsExporter.class);
        pm.registerExporter(EpsExporter.class);
        pm.registerExporter(PngExporter.class);
    }

    @Override
    public String getDescription() {
        return "Built-in exporters for Workcraft models";
    }

}
