package org.workcraft.plugins.policy;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PolicyNetModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, PolicyNetModelDescriptor.class);
		framework.getPluginManager().registerClass(XMLSerialiser.class, BundleSerialiser.class);
		framework.getPluginManager().registerClass(XMLDeserialiser.class, BundleDeserialiser.class);
	}

	@Override
	public String getDescription() {
		return "Policy Net";
	}

}
