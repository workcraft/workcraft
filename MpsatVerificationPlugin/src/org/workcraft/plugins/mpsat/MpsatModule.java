package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.tools.CscResolutionTool;
import org.workcraft.plugins.mpsat.tools.MpsatCombinedChecker;
import org.workcraft.plugins.mpsat.tools.MpsatConformationChecker;
import org.workcraft.plugins.mpsat.tools.MpsatConsistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatCscChecker;
import org.workcraft.plugins.mpsat.tools.MpsatReachPropertyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatSvaPropertyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDeadlockChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDiInterfaceChecker;
import org.workcraft.plugins.mpsat.tools.MpsatInputPropernessChecker;
import org.workcraft.plugins.mpsat.tools.MpsatNormalcyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatOutputPersistencyChecker;
import org.workcraft.plugins.mpsat.tools.MpsatUscChecker;

public class MpsatModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Tool.class, CscResolutionTool.class);
        pm.registerClass(Tool.class, MpsatConsistencyChecker.class);
        pm.registerClass(Tool.class, MpsatDeadlockChecker.class);
        pm.registerClass(Tool.class, MpsatInputPropernessChecker.class);
        pm.registerClass(Tool.class, MpsatOutputPersistencyChecker.class);
        pm.registerClass(Tool.class, MpsatDiInterfaceChecker.class);
        pm.registerClass(Tool.class, MpsatNormalcyChecker.class);
        pm.registerClass(Tool.class, MpsatCscChecker.class);
        pm.registerClass(Tool.class, MpsatUscChecker.class);
        pm.registerClass(Tool.class, MpsatConformationChecker.class);
        pm.registerClass(Tool.class, MpsatCombinedChecker.class);
        pm.registerClass(Tool.class, MpsatReachPropertyChecker.class);
        pm.registerClass(Tool.class, MpsatSvaPropertyChecker.class);
        pm.registerClass(Settings.class, MpsatUtilitySettings.class);
    }

    @Override
    public String getDescription() {
        return "MPSat verification support";
    }

}
