package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tools.ShowSg;

public class PetrifyExtraModule implements Module {

    private final class ShowBinarySg extends ShowSg {
        @Override
        public boolean isBinary() {
            return true;
        }
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(Exporter.class, AstgExporter.class);

        pm.registerClass(Tool.class, ShowSg.class);

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new ShowBinarySg();
            }
        });
    }

    @Override
    public String getDescription() {
        return "Petrify state graph support";
    }

}
