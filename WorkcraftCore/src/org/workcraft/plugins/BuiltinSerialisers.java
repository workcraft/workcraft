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
import org.workcraft.plugins.serialisation.xml.ColorDeserialiser;
import org.workcraft.plugins.serialisation.xml.ColorSerialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.FileDeserialiser;
import org.workcraft.plugins.serialisation.xml.FileSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualReplicaDeserialiser;
import org.workcraft.plugins.serialisation.xml.VisualReplicaSerialiser;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class BuiltinSerialisers implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDeserialiser.class, new Initialiser<ModelDeserialiser>() {
            public ModelDeserialiser create() {
                return new XMLModelDeserialiser(framework.getPluginManager());
            }
        });
        pm.registerClass(ModelSerialiser.class, new Initialiser<ModelSerialiser>() {
            public ModelSerialiser create() {
                return new XMLModelSerialiser(framework.getPluginManager());
            }
        });

        pm.registerClass(XMLSerialiser.class, AffineTransformSerialiser.class);
        pm.registerClass(XMLSerialiser.class, BooleanSerialiser.class);
        pm.registerClass(XMLSerialiser.class, DoubleSerialiser.class);
        pm.registerClass(XMLSerialiser.class, EnumSerialiser.class);
        pm.registerClass(XMLSerialiser.class, IntSerialiser.class);
        pm.registerClass(XMLSerialiser.class, ColorSerialiser.class);
        pm.registerClass(XMLSerialiser.class, StringSerialiser.class);
        pm.registerClass(XMLSerialiser.class, BezierSerialiser.class);
        pm.registerClass(XMLSerialiser.class, ConnectionSerialiser.class);
        pm.registerClass(XMLSerialiser.class, VisualConnectionSerialiser.class);
        pm.registerClass(XMLSerialiser.class, VisualReplicaSerialiser.class);
        pm.registerClass(XMLSerialiser.class, FileSerialiser.class);

        pm.registerClass(XMLDeserialiser.class, AffineTransformDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, BooleanDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, DoubleDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, EnumDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, IntDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, ColorDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, StringDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, BezierDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, ConnectionDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, VisualConnectionDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, VisualReplicaDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, FileDeserialiser.class);
    }

    @Override
    public String getDescription() {
        return "Built-in XML serialisers for basic data types";
    }
}
