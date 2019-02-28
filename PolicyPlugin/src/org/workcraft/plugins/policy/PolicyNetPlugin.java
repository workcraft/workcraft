package org.workcraft.plugins.policy;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.policy.commands.BundleTransitionTransformationCommand;
import org.workcraft.plugins.policy.commands.PetriToPolicyConversionCommand;
import org.workcraft.plugins.policy.commands.PolicyDeadlockFreenessVerificationCommand;
import org.workcraft.plugins.policy.commands.PolicyToPetriConversionCommand;
import org.workcraft.plugins.policy.serialisation.BundleDeserialiser;
import org.workcraft.plugins.policy.serialisation.BundleSerialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalityDeserialiser;
import org.workcraft.plugins.policy.serialisation.VisualLocalitySerialiser;

@SuppressWarnings("unused")
public class PolicyNetPlugin implements Plugin {

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

        pm.registerModelDescriptor(PolicyNetDescriptor.class);
        pm.registerXmlSerialiser(BundleSerialiser.class);
        pm.registerXmlDeserialiser(BundleDeserialiser.class);
        pm.registerXmlSerialiser(VisualLocalitySerialiser.class);
        pm.registerXmlDeserialiser(VisualLocalityDeserialiser.class);

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
