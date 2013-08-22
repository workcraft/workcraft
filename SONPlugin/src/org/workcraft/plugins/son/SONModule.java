package org.workcraft.plugins.son;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.son.serialisation.VisualONGroupDeserialiser;
import org.workcraft.plugins.son.serialisation.VisualONGroupSerialiser;
import org.workcraft.plugins.son.serialisation.SONConnectionDeserialiser;
import org.workcraft.plugins.son.serialisation.SONConnectionSerialiser;
import org.workcraft.plugins.son.serialisation.VisualSONConnectionDeserialiser;
import org.workcraft.plugins.son.serialisation.VisualSONConnectionSerialiser;
import org.workcraft.plugins.son.tools.ColorRefreshTool;
import org.workcraft.plugins.son.tools.StructurePropertyChecker;
import org.workcraft.plugins.son.tools.TestTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;


public class SONModule implements Module{

	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, SONModelDescriptor.class);
		framework.getPluginManager().registerClass(Tool.class, TestTool.class, framework);
		framework.getPluginManager().registerClass(Tool.class, StructurePropertyChecker.class, framework);
		framework.getPluginManager().registerClass(Tool.class, ColorRefreshTool.class);

		framework.getPluginManager().registerClass(XMLSerialiser.class, SONConnectionSerialiser.class);
		framework.getPluginManager().registerClass(XMLSerialiser.class, VisualSONConnectionSerialiser.class);
		framework.getPluginManager().registerClass(XMLSerialiser.class, VisualONGroupSerialiser.class);

		framework.getPluginManager().registerClass(XMLDeserialiser.class,VisualONGroupDeserialiser.class);
		framework.getPluginManager().registerClass(XMLDeserialiser.class, SONConnectionDeserialiser.class);
		framework.getPluginManager().registerClass(XMLDeserialiser.class, VisualSONConnectionDeserialiser.class);

	}

	public String getDescription() {
		return "Structured Occurrence Nets";
	}
}
