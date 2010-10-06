package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformDeserialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
import org.workcraft.plugins.serialisation.xml.BezierDeserialiser;
import org.workcraft.plugins.serialisation.xml.BezierSerialiser;
import org.workcraft.plugins.serialisation.xml.BooleanDeserialiser;
import org.workcraft.plugins.serialisation.xml.BooleanSerialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class BuiltinSerialisers implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDeserialiser.class, new Initialiser<ModelDeserialiser>(){public ModelDeserialiser create(){return new XMLModelDeserialiser(framework.getPluginManager());}});
		p.registerClass(ModelSerialiser.class, new Initialiser<ModelSerialiser>(){public ModelSerialiser create(){return new XMLModelSerialiser(framework.getPluginManager());}});

		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, AffineTransformSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, BooleanSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, DoubleSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, EnumSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, IntSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, StringSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, BezierSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, ConnectionSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, VisualConnectionSerialiser.class);

		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, AffineTransformDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, BooleanDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, DoubleDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, EnumDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, IntDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, StringDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, BezierDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, ConnectionDeserialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.class, VisualConnectionDeserialiser.class);
	}

	@Override
	public String getDescription() {
		return "Built-in XML serialisers for basic data types";
	}
}
