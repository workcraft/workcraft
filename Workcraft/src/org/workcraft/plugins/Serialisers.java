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
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
				AffineTransformSerialiser.class,
				BooleanSerialiser.class,
				DoubleSerialiser.class,
				EnumSerialiser.class,
				IntSerialiser.class,
				StringSerialiser.class,
				BezierSerialiser.class,
				ConnectionSerialiser.class,
				ImplicitPlaceArcSerialiser.class,
				VisualConnectionSerialiser.class,

				DotGSerialiser.class,
		};
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDeserialiser.class, new Initialiser<ModelDeserialiser>(){public ModelDeserialiser create(){return new XMLDeserialiser(framework.getPluginManager());}});
		p.registerClass(ModelSerialiser.class, new Initialiser<ModelSerialiser>(){public ModelSerialiser create(){return new XMLSerialiser(framework.getPluginManager());}});
	}

}
