package org.workcraft.plugins.pcomp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

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

    private final LinkedHashMap<String, ComponentData> fileToComponent = new LinkedHashMap<>();

    public CompositionData(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public CompositionData(InputStream is) {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return;
        }

        Element root = doc.getDocumentElement();
        NodeList components = root.getChildNodes();
        for (int i = 0; i < components.getLength(); i++) {
            Node item = components.item(i);
            if (!(item instanceof Element)) continue;
            Element element = (Element) item;
            if ("STG".equals(element.getTagName())) {
                Element fileElement = XmlUtils.getChildElement("file", element);
                Element placesElement = XmlUtils.getChildElement("places", element);
                Element transitionsElement = XmlUtils.getChildElement("transitions", element);
                ComponentData componentData = new ComponentData(fileElement, placesElement, transitionsElement);
                fileToComponent.put(componentData.getFileName(), componentData);
            }
        }
    }

    public ComponentData getComponentData(File file) {
        return getComponentData(file.getAbsolutePath());
    }

    public ComponentData getComponentData(String fileName) {
        return fileToComponent.get(fileName);
    }

    public ComponentData getComponentData(int index) {
        ArrayList<ComponentData> components = new ArrayList<>(fileToComponent.values());
        return components.get(index);
    }

    public Set<String> getFileNames() {
        return new HashSet<>(fileToComponent.keySet());
    }

}
