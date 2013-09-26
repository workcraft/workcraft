package org.workcraft.plugins.policy;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.policy.tools.PetriNetGeneratorTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PolicyNetModule implements Module {

	@Override
	public void init(final Framework framework) {
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new PetriNetGeneratorTool(framework);
			}
		});

		pm.registerClass(ModelDescriptor.class, PolicyNetModelDescriptor.class);
		pm.registerClass(XMLSerialiser.class, BundleSerialiser.class);
		pm.registerClass(XMLDeserialiser.class, BundleDeserialiser.class);
	}

	@Override
	public String getDescription() {
		return "Policy Net";
	}

}
