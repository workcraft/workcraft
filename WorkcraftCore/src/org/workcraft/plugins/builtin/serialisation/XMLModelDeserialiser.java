package org.workcraft.plugins.builtin.serialisation;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.WorkMathFormat;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.References;
import org.workcraft.serialisation.XMLDeserialisationManager;
import org.workcraft.utils.XmlUtils;
import org.xml.sax.SAXException;

public class XMLModelDeserialiser implements ModelDeserialiser {

    private final PluginProvider plugins;

    public XMLModelDeserialiser(PluginProvider plugins) {
        this.plugins = plugins;
    }

    @Override
    public UUID getFormatUUID() {
        return WorkMathFormat.getInstance().getUuid();
    }

    @Override
    public DeserialisationResult deserialise(InputStream is, ReferenceResolver extRef,
            Model underlyingModel) throws DeserialisationException {

        try {
            XMLDeserialisationManager deserialisation = new XMLDeserialisationManager();
            deserialisation.processPlugins(plugins);

            Document doc = XmlUtils.loadDocument(is);
            Element modelElement = doc.getDocumentElement();

            // create model
            String modelClassName = modelElement.getAttribute("class");
            if (modelClassName == null || modelClassName.isEmpty()) {
                throw new DeserialisationException("Class name attribute is not set\n" + modelElement.toString());
            }

            deserialisation.begin(extRef);

            // 1st pass -- init instances
            Element rootElement = XmlUtils.getChildElement("root", modelElement);
            Node root = (Node) deserialisation.initInstance(rootElement);

            // 2nd pass -- finalise instances
            deserialisation.finaliseInstances();
            Class<?> cls = Class.forName(modelClassName);

            References intRef = deserialisation.getReferenceResolver();
            Model model = XMLDeserialisationManager.createModel(cls, root, underlyingModel, intRef);
            deserialisation.deserialiseModelProperties(modelElement, model);

            return new DeserialisationResult(model, intRef);
        } catch (ParserConfigurationException | SAXException | IOException |
                SecurityException | IllegalArgumentException | ClassNotFoundException e) {
            throw new DeserialisationException(e);
        }
    }

}
