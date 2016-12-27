package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.XMLSerialisationManager;
import org.workcraft.util.XmlUtil;

public class XMLModelSerialiser implements ModelSerialiser {
    XMLSerialisationManager serialisation = new XMLSerialisationManager();

    public XMLModelSerialiser(PluginProvider mock) {
        serialisation.processPlugins(mock);
    }

    public String getDescription() {
        return "Workcraft XML serialiser";
    }

    public boolean isApplicableTo(Model model) {
        return true;
    }

    public boolean isApplicableTo(VisualModel model) {
        return true;
    }

    public UUID getFormatUUID() {
        return Format.workcraftXML;
    }

    public String getExtension() {
        return ".xml";
    }

    public ReferenceProducer serialise(final Model model, OutputStream out, ReferenceProducer refs)
            throws SerialisationException {
        try {

            ReferenceProducer internalRefs = new ReferenceProducer() {
                public String getReference(Object obj) {
                    if (obj instanceof Node) {
                        return model.getNodeReference((Node) obj);
                    } else {
                        return null;
                    }
                }
            };

            Document doc = XmlUtil.createDocument();

            Element modelElement = doc.createElement("model");
            Element rootElement = doc.createElement("root");

            serialisation.begin(internalRefs, refs);

            serialisation.serialise(modelElement, model);
            serialisation.serialise(rootElement, model.getRoot());

            serialisation.end();

            doc.appendChild(modelElement);
            modelElement.appendChild(rootElement);
            XmlUtil.writeDocument(doc, out);

            return internalRefs;
        } catch (ParserConfigurationException e) {
            throw new SerialisationException(e);
        } catch (IOException e) {
            throw new SerialisationException(e);
        }
    }
}
