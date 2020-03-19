package org.workcraft.plugins.builtin;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.interop.*;

@SuppressWarnings("unused")
public class BuiltinExporters implements Plugin {

    @Override
    public String getDescription() {
        return "Built-in exporters";
    }

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

}
