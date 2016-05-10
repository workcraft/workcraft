package org.workcraft.plugins.petrify;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.petrify.tools.PetrifyCscConflictResolution;
import org.workcraft.plugins.petrify.tools.PetrifyDummyContraction;
import org.workcraft.plugins.petrify.tools.PetrifyNetSynthesis;
import org.workcraft.plugins.petrify.tools.PetrifyNetSynthesisHide;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisComplexGate;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisGeneralisedCelement;
import org.workcraft.plugins.petrify.tools.PetrifySynthesisTechnologyMapping;
import org.workcraft.plugins.petrify.tools.PetrifyUntoggle;

public class PetrifyModule implements Module {

    private final class PetrifyNetSynthesisEr extends PetrifyNetSynthesis {
        @Override
        public String getDisplayName() {
            return "Net synthesis [Petrify with -er option]";
        }

        @Override
        public ArrayList<String> getArgs() {
            ArrayList<String> args = super.getArgs();
            args.add("-er");
            return args;
        }
    }

    private final class PetrifyNetSynthesisHideEr extends PetrifyNetSynthesisHide {
        @Override
        public String getDisplayName() {
            return "Net synthesis hiding selected signals and dummies [Petrify with -er option]";
        }

        @Override
        public ArrayList<String> getArgs() {
            ArrayList<String> args = super.getArgs();
            args.add("-er");
            return args;
        }
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, PetrifyUtilitySettings.class);

        pm.registerClass(Tool.class, PetrifyUntoggle.class);
        pm.registerClass(Tool.class, PetrifyCscConflictResolution.class);
        pm.registerClass(Tool.class, PetrifySynthesisComplexGate.class);
        pm.registerClass(Tool.class, PetrifySynthesisGeneralisedCelement.class);
        pm.registerClass(Tool.class, PetrifySynthesisTechnologyMapping.class);
        pm.registerClass(Tool.class, PetrifyDummyContraction.class);
        pm.registerClass(Tool.class, PetrifyNetSynthesis.class);
        pm.registerClass(Tool.class, PetrifyNetSynthesisHide.class);

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new PetrifyNetSynthesisEr();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new PetrifyNetSynthesisHideEr();
            }
        });
    }

    @Override
    public String getDescription() {
        return "Petrify synthesis support";
    }

}
