package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.utils.XmlUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class XMLSerialisationManager implements SerialiserFactory, NodeSerialiser {

    private final HashMap<String, XMLSerialiser> serialisers = new HashMap<>();
    private final DefaultNodeSerialiser nodeSerialiser = new DefaultNodeSerialiser(this, this);
    private XMLSerialiserState state = null;

    private void registerSerialiser(XMLSerialiser serialiser) {
        serialisers.put(serialiser.getClassName(), serialiser);
    }

    @Override
    public XMLSerialiser getSerialiserFor(Class<?> cls) {
        return serialisers.get(cls.getName());
    }

    public void begin(ReferenceProducer internalReferenceResolver, ReferenceProducer externalReferenceResolver) {
        state = new XMLSerialiserState(internalReferenceResolver, externalReferenceResolver);
    }

    public void end() {
        state = null;
    }

    public void processPlugins(PluginProvider pp) {
        for (PluginInfo<? extends XMLSerialiser> info : pp.getPlugins(XMLSerialiser.class)) {
            registerSerialiser(info.getInstance());
        }
    }

    @Override
    public void serialise(Element element, Object object) throws SerialisationException {
        element.setAttribute(XMLCommonAttributes.CLASS_ATTRIBUTE, object.getClass().getName());

        nodeSerialiser.serialise(element, object, state.internalReferences, state.externalReferences);

        if (object instanceof Container container) {
            ArrayList<Node> children = new ArrayList<>(container.getChildren());
            for (Node child : children) {
                Element childElement = XmlUtils.createChildElement(XMLCommonAttributes.NODE_ATTRIBUTE, element);
                serialise(childElement, child);
            }
        }
    }

}
