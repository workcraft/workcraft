package org.workcraft.plugins.policy;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.policy.serialisation.BundleDeserialiser;
import org.workcraft.plugins.policy.serialisation.BundleSerialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalityDeserialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalitySerialiser;
import org.workcraft.plugins.policy.tools.CheckDeadlockTool;
import org.workcraft.plugins.policy.tools.PetriNetGeneratorTool;
import org.workcraft.plugins.policy.tools.PetriNetToPolicyNetConverterTool;
import org.workcraft.plugins.policy.tools.TransitionBundlerTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PolicyNetModule implements Module {

    @Override
    public String getDescription() {
        return "Policy Net";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new PetriNetGeneratorTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new TransitionBundlerTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new CheckDeadlockTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new PetriNetToPolicyNetConverterTool();
            }
        });

        pm.registerClass(ModelDescriptor.class, PolicyNetDescriptor.class);
        pm.registerClass(XMLSerialiser.class, BundleSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, BundleDeserialiser.class);
        pm.registerClass(XMLSerialiser.class, VisualLocalitySerialiser.class);
        pm.registerClass(XMLDeserialiser.class,VisualLocalityDeserialiser.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.policy.PolicyNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.policy.PolicyNetDescriptor\"/>");
    }

}
