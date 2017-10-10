package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.interop.DotExporter;
import org.workcraft.plugins.interop.EpsExporter;
import org.workcraft.plugins.interop.PdfExporter;
import org.workcraft.plugins.interop.PngExporter;
import org.workcraft.plugins.interop.PsExporter;
import org.workcraft.plugins.interop.SvgExporter;

public class BuiltinExporters implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(Exporter.class, DotExporter.class);
        pm.registerClass(Exporter.class, SvgExporter.class);
        pm.registerClass(Exporter.class, PdfExporter.class);
        pm.registerClass(Exporter.class, PsExporter.class);
        pm.registerClass(Exporter.class, EpsExporter.class);
        pm.registerClass(Exporter.class, PngExporter.class);
    }

    @Override
    public String getDescription() {
        return "Built-in exporters for Workcraft models";
    }

}
