package org.workcraft.plugins.pcomp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.workcraft.util.XmlUtils;
import org.xml.sax.SAXException;

public class CompositionData {

    HashMap<String, ComponentData> fileToComponent = new HashMap<>();

    public CompositionData(File file) {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            return;
        }

        Element root = doc.getDocumentElement();
        NodeList components = root.getChildNodes();
        for (int i = 0; i < components.getLength(); i++) {
            Node item = components.item(i);
            if (!(item instanceof Element)) continue;
            Element element = (Element) item;
            if ("STG".equals(element.getTagName())) {
                Element placesElement = XmlUtils.getChildElement("places", element);
                Element transitionsElement = XmlUtils.getChildElement("transitions", element);
                ComponentData componentData = new ComponentData(placesElement, transitionsElement);
                Element fileElement = XmlUtils.getChildElement("file", element);
                String fileName = fileElement.getTextContent();
                fileToComponent.put(fileName, componentData);
            }
        }
    }

    public ComponentData getComponentData(File file) {
        return getComponentData(file.getAbsolutePath());
    }

    public ComponentData getComponentData(String fileName) {
        return fileToComponent.get(fileName);
    }

}
