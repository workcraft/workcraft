package org.workcraft.plugins.policy;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.policy.commands.BundleTransitionTransformationCommand;
import org.workcraft.plugins.policy.commands.PetriToPolicyConversionCommand;
import org.workcraft.plugins.policy.commands.PolicyDeadlockFreenessVerificationCommand;
import org.workcraft.plugins.policy.commands.PolicyToPetriConversionCommand;
import org.workcraft.plugins.policy.serialisation.BundleDeserialiser;
import org.workcraft.plugins.policy.serialisation.BundleSerialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalityDeserialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalitySerialiser;
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

        pm.registerClass(ModelDescriptor.class, PolicyNetDescriptor.class);
        pm.registerClass(XMLSerialiser.class, BundleSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, BundleDeserialiser.class);
        pm.registerClass(XMLSerialiser.class, VisualLocalitySerialiser.class);
        pm.registerClass(XMLDeserialiser.class, VisualLocalityDeserialiser.class);

        ScriptableCommandUtils.register(BundleTransitionTransformationCommand.class, "transformPolicyBundleTransitions",
                "transform the given Policy net 'work' by bundling selected transition");

        ScriptableCommandUtils.register(PolicyDeadlockFreenessVerificationCommand.class, "checkPolicyDeadlockFreeness",
                "check the Policy net 'work' for deadlock freeness");

        ScriptableCommandUtils.register(PolicyToPetriConversionCommand.class, "convertPolicyToPetri",
                "convert the given Policy net 'work' into a new Petri net work");
        ScriptableCommandUtils.register(PetriToPolicyConversionCommand.class, "convertPetriToPolicy",
                "convert the given Petri net 'work' into a new Policy net work");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.policy.PolicyNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.policy.PolicyNetDescriptor\"/>");
    }

}
