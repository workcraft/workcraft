package org.workcraft.testing.serialisation.xml;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Group;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginProvider;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.serialisation.xml.AffineTransformDeserialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
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
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class XMLSerialisationTestingUtils {
	static class MockPluginManager implements PluginProvider {
		public PluginInfo[] getPluginsImplementing(String interfaceName) {

			if (interfaceName.equals(org.workcraft.framework.serialisation.xml.XMLSerialiser.class.getName()))
			{
				return new PluginInfo[] {
						new PluginInfo (IntSerialiser.class),
						new PluginInfo (BooleanSerialiser.class),
						new PluginInfo (StringSerialiser.class),
						new PluginInfo (DoubleSerialiser.class),
						new PluginInfo (ConnectionSerialiser.class),
						new PluginInfo (IntSerialiser.class),
						new PluginInfo (EnumSerialiser.class),
						new PluginInfo (AffineTransformSerialiser.class)
				};
			} else if (interfaceName.equals(org.workcraft.framework.serialisation.xml.XMLDeserialiser.class.getName()))
			{
				return new PluginInfo[] {
						new PluginInfo (IntDeserialiser.class),
						new PluginInfo (BooleanDeserialiser.class),
						new PluginInfo (StringDeserialiser.class),
						new PluginInfo (DoubleDeserialiser.class),
						new PluginInfo (ConnectionDeserialiser.class),
						new PluginInfo (IntDeserialiser.class),
						new PluginInfo (EnumDeserialiser.class),
						new PluginInfo (AffineTransformDeserialiser.class)
				};
			} else
				throw new RuntimeException ("Mock plugin manager doesn't know interface " + interfaceName);
		}
	}

	public static PluginProvider createMockPluginManager() {
		return new MockPluginManager();
	}

	public static STG createTestSTG1() {
		try {
			STG stg = new STG();

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	public static STG createTestSTG2() {
		try {
			STG stg = new STG();

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			Group g1 = new Group();

			Place p3 = stg.createPlace();
			SignalTransition t3 = stg.createSignalTransition();
			Connection con = stg.connect(p3, t3);

			g1.add(p3);
			g1.add(t3);
			g1.add(con);

			stg.getRoot().add(g1);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}
}