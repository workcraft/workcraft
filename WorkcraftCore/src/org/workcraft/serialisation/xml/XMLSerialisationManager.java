package org.workcraft.serialisation.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.XmlUtil;

public class XMLSerialisationManager implements SerialiserFactory, NodeSerialiser {
    private final HashMap<String, XMLSerialiser> serialisers = new HashMap<>();
    private final DefaultNodeSerialiser nodeSerialiser = new DefaultNodeSerialiser(this, this);
    private XMLSerialiserState state = null;

    private void registerSerialiser(XMLSerialiser serialiser) {
        serialisers.put(serialiser.getClassName(), serialiser);
    }

    public XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException {
        return serialisers.get(cls.getName());
    }

    public void begin(ReferenceProducer internalReferenceResolver, ReferenceProducer externalReferenceResolver) {
        state = new XMLSerialiserState(internalReferenceResolver, externalReferenceResolver);
    }

    public void end() {
        state = null;
    }

    public void processPlugins(PluginProvider manager) {
        for (PluginInfo<? extends XMLSerialiser> info : manager.getPlugins(XMLSerialiser.class)) {
            final XMLSerialiser newInstance = info.newInstance();
            registerSerialiser(newInstance);
        }
    }

    public void serialise(Element element, Object object) throws SerialisationException {
        element.setAttribute("class", object.getClass().getName());

        nodeSerialiser.serialise(element, object, state.internalReferences, state.externalReferences);

        if (object instanceof Container) {
            Container container = (Container) object;
            ArrayList<Node> children = new ArrayList<>(container.getChildren());
            for (Node child : children) {
                Element childElement = XmlUtil.createChildElement("node", element);
                serialise(childElement, child);
            }
        }
    }
}
