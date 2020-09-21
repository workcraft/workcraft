package org.workcraft.plugins.pcomp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.workcraft.utils.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class CompositionData {

    private static final String STG_ELEMENT_NAME = "STG";
    private static final String FILE_ELEMENT_NAME = "file";
    private static final String PLACES_ELEMENT_NAME = "places";
    private static final String TRANSITIONS_ELEMENT_NAME = "transitions";


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
            if (item instanceof Element) {
                Element element = (Element) item;
                if (STG_ELEMENT_NAME.equals(element.getTagName())) {
                    Element fileElement = XmlUtils.getChildElement(FILE_ELEMENT_NAME, element);
                    Element placesElement = XmlUtils.getChildElement(PLACES_ELEMENT_NAME, element);
                    Element transitionsElement = XmlUtils.getChildElement(TRANSITIONS_ELEMENT_NAME, element);
                    ComponentData componentData = new ComponentData(fileElement, placesElement, transitionsElement);
                    fileToComponent.put(componentData.getFileName(), componentData);
                }
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
        return (index >= components.size()) ? null : components.get(index);
    }

    public Set<String> getFileNames() {
        return new HashSet<>(fileToComponent.keySet());
    }

}
