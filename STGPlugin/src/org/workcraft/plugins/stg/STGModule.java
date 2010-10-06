package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.serialisation.DotGSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class STGModule implements Module {

	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDescriptor.class, STGModelDescriptor.class);

		p.registerClass(XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
		p.registerClass(XMLDeserialiser.class, ImplicitPlaceArcDeserialiser.class);

		p.registerClass(Exporter.class, DotGExporter.class);
		p.registerClass(Importer.class, DotGImporter.class);

		p.registerClass(ModelSerialiser.class, DotGSerialiser.class);

	}

	@Override
	public String getDescription() {
		return "Signal Transition Graphs";
	}
}
