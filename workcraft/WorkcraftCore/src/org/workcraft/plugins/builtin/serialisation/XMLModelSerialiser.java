package org.workcraft.plugins.builtin.serialisation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.WorkMathFormat;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.XMLSerialisationManager;
import org.workcraft.utils.XmlUtils;

import java.io.OutputStream;
import java.util.UUID;

public class XMLModelSerialiser implements ModelSerialiser {

    private final XMLSerialisationManager serialisation = new XMLSerialisationManager();

    public XMLModelSerialiser(PluginProvider pp) {
        serialisation.processPlugins(pp);
    }

    @Override
    public UUID getFormatUUID() {
        return WorkMathFormat.getInstance().getUuid();
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return true;
    }

    @Override
    public ReferenceProducer serialise(final Model model, OutputStream out, ReferenceProducer refs)
            throws SerialisationException {

        ReferenceProducer internalRefs = obj -> {
            if (obj instanceof Node) {
                return model.getNodeReference((Node) obj);
            }
            return null;
        };

        Document doc = XmlUtils.createDocument();
        Element modelElement = doc.createElement("model");
        Element rootElement = doc.createElement("root");

        serialisation.begin(internalRefs, refs);

        serialisation.serialise(modelElement, model);
        serialisation.serialise(rootElement, model.getRoot());

        serialisation.end();

        doc.appendChild(modelElement);
        modelElement.appendChild(rootElement);
        XmlUtils.writeDocument(doc, out);

        return internalRefs;
    }

}
