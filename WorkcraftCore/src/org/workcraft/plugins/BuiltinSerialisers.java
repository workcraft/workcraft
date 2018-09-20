package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.serialisation.xml.*;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class BuiltinSerialisers implements Module {
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDeserialiser.class, () -> new XMLModelDeserialiser(framework.getPluginManager()));
        pm.registerClass(ModelSerialiser.class, () -> new XMLModelSerialiser(framework.getPluginManager()));

        pm.registerXmlSerialiser(AffineTransformSerialiser.class);
        pm.registerXmlSerialiser(BooleanSerialiser.class);
        pm.registerXmlSerialiser(DoubleSerialiser.class);
        pm.registerXmlSerialiser(EnumSerialiser.class);
        pm.registerXmlSerialiser(IntSerialiser.class);
        pm.registerXmlSerialiser(ColorSerialiser.class);
        pm.registerXmlSerialiser(StringSerialiser.class);
        pm.registerXmlSerialiser(BezierSerialiser.class);
        pm.registerXmlSerialiser(ConnectionSerialiser.class);
        pm.registerXmlSerialiser(VisualConnectionSerialiser.class);
        pm.registerXmlSerialiser(VisualReplicaSerialiser.class);
        pm.registerXmlSerialiser(FileSerialiser.class);

        pm.registerXmlDeserialiser(AffineTransformDeserialiser.class);
        pm.registerXmlDeserialiser(BooleanDeserialiser.class);
        pm.registerXmlDeserialiser(DoubleDeserialiser.class);
        pm.registerXmlDeserialiser(EnumDeserialiser.class);
        pm.registerXmlDeserialiser(IntDeserialiser.class);
        pm.registerXmlDeserialiser(ColorDeserialiser.class);
        pm.registerXmlDeserialiser(StringDeserialiser.class);
        pm.registerXmlDeserialiser(BezierDeserialiser.class);
        pm.registerXmlDeserialiser(ConnectionDeserialiser.class);
        pm.registerXmlDeserialiser(VisualConnectionDeserialiser.class);
        pm.registerXmlDeserialiser(VisualReplicaDeserialiser.class);
        pm.registerXmlDeserialiser(FileDeserialiser.class);
    }

    @Override
    public String getDescription() {
        return "Built-in XML serialisers for basic data types";
    }

}
