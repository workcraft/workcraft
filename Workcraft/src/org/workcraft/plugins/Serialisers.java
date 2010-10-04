package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.serialisation.XMLDeserialiser;
import org.workcraft.plugins.serialisation.XMLSerialiser;
import org.workcraft.plugins.serialisation.dotg.DotGSerialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
import org.workcraft.plugins.serialisation.xml.BezierSerialiser;
import org.workcraft.plugins.serialisation.xml.BooleanSerialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class Serialisers implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDeserialiser.class, new Initialiser<ModelDeserialiser>(){public ModelDeserialiser create(){return new XMLDeserialiser(framework.getPluginManager());}});
		p.registerClass(ModelSerialiser.class, new Initialiser<ModelSerialiser>(){public ModelSerialiser create(){return new XMLSerialiser(framework.getPluginManager());}});

		p.registerClass(ModelSerialiser.class, DotGSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, AffineTransformSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, BooleanSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, DoubleSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, EnumSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, IntSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, StringSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, BezierSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, ConnectionSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.class, VisualConnectionSerialiser.class);
	}
}
