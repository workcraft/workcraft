package org.workcraft.plugins.cpog;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.PropertyClassProvider;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.cpog.serialisation.ArcDeserialiser;
import org.workcraft.plugins.cpog.serialisation.ArcSerialiser;
import org.workcraft.plugins.cpog.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.cpog.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.plugins.cpog.serialisation.RhoClauseDeserialiser;
import org.workcraft.plugins.cpog.serialisation.RhoClauseSerialiser;
import org.workcraft.plugins.cpog.serialisation.VertexDeserialiser;
import org.workcraft.plugins.cpog.serialisation.VertexSerialiser;
import org.workcraft.plugins.cpog.serialisation.VisualCPOGGroupDeserialiser;
import org.workcraft.plugins.cpog.serialisation.VisualCPOGGroupSerialiser;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class CpogModule implements Module {
	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();

		p.registerClass(ModelDescriptor.class, CpogModelDescriptor.class);

		p.registerClass(PropertyClassProvider.class, EncodingPropertyProvider.class);

		p.registerClass(XMLSerialiser.class, VisualCPOGGroupSerialiser.class);
		p.registerClass(XMLSerialiser.class, VertexSerialiser.class);
		p.registerClass(XMLSerialiser.class, RhoClauseSerialiser.class);
		p.registerClass(XMLSerialiser.class, ArcSerialiser.class);

		p.registerClass(XMLDeserialiser.class, VisualCPOGGroupDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, VertexDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, RhoClauseDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, ArcDeserialiser.class);

		p.registerClass(Tool.class, CpogEncoder.class);
	}

	@Override
	public String getDescription() {
		return "Conditional Partial Order Graphs";
	}
}