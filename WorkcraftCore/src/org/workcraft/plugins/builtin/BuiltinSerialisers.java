package org.workcraft.plugins.builtin;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.builtin.serialisation.xml.*;

@SuppressWarnings("unused")
public class BuiltinSerialisers implements Plugin {

    @Override
    public String getDescription() {
        return "Built-in serialisers";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDeserialiser(() -> new XMLModelDeserialiser(framework.getPluginManager()));
        pm.registerModelSerialiser(() -> new XMLModelSerialiser(framework.getPluginManager()));

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
        pm.registerXmlSerialiser(FileReferenceSerialiser.class);
        pm.registerXmlSerialiser(AltFileReferenceSerialiser.class);

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
        pm.registerXmlDeserialiser(FileReferenceDeserialiser.class);
        pm.registerXmlDeserialiser(AltFileReferenceDeserialiser.class);
    }

}
