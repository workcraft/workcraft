package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisComplexGate;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisGeneralisedCelement;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisStandardCelement;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesisTechnologyMapping;

public class MpsatSynthesisModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Tool.class, MpsatSynthesisComplexGate.class);
        pm.registerClass(Tool.class, MpsatSynthesisGeneralisedCelement.class);
        pm.registerClass(Tool.class, MpsatSynthesisStandardCelement.class);
        pm.registerClass(Tool.class, MpsatSynthesisTechnologyMapping.class);
        pm.registerClass(Settings.class, MpsatSynthesisUtilitySettings.class);
    }

    @Override
    public String getDescription() {
        return "MPSat synthesis support";
    }
}
