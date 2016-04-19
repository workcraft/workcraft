package org.workcraft.plugins.pog;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.pog.serialisation.VertexDeserialiser;
import org.workcraft.plugins.pog.serialisation.VertexSerialiser;
import org.workcraft.plugins.pog.tools.PogToPnConverterTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PogModule implements Module {

    @Override
    public String getDescription() {
        return "Partial Order Graphs";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, PogDescriptor.class);

        pm.registerClass(XMLSerialiser.class, VertexSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, VertexDeserialiser.class);

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new PogToPnConverterTool();
            }
        });
    }

}
