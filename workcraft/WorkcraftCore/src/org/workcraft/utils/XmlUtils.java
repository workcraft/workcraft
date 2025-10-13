package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class XmlUtils {

    private static final String LIST_ELEMENT = "list";
    private static final String ITEM_ELEMENT = "item";

    private static String getAttribute(Element element, String attributeName) {
        return (element == null) || !element.hasAttribute(attributeName) ? null : element.getAttribute(attributeName);
    }

    public static String readTextAttribute(Element element, String attributeName, String defaultValue) {
        String attribute = getAttribute(element, attributeName);
        return attribute == null ? defaultValue : attribute;
    }

    public static int readIntAttribute(Element element, String attributeName, int defaultValue) {
        return ParseUtils.parseInt(getAttribute(element, attributeName), defaultValue);
    }

    public static boolean readBooleanAttribute(Element element, String attributeName, boolean defaultValue) {
        return ParseUtils.parseBoolean(getAttribute(element, attributeName), defaultValue);
    }

    public static <T extends Enum<T>> T readEnumAttribute(Element element, String attributeName, Class<T> enumType, T defaultValue) {
        return ParseUtils.parseEnum(getAttribute(element, attributeName), enumType, defaultValue);
    }

    public static List<Element> getChildElements(String elementName, Element parent) {
        LinkedList<Element> result = new LinkedList<>();
        if (parent != null) {
            NodeList nodes = parent.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if ((node.getNodeType() == Node.ELEMENT_NODE) && elementName.equals(node.getNodeName())) {
                    result.add((Element) node);
                }
            }
        }
        return result;
    }

    public static Element getChildElement(String elementName, Element parent) {
        if (parent != null) {
            NodeList nodes = parent.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if ((node.getNodeType() == Node.ELEMENT_NODE) && elementName.equals(node.getNodeName())) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    public static Element createChildElement(String elementName, Document document) {
        Element result = document.createElement(elementName);
        document.appendChild(result);
        return result;
    }

    public static Element createChildElement(String elementName, Element parent) {
        Document document = parent.getOwnerDocument();
        Element result = document.createElement(elementName);
        parent.appendChild(result);
        return result;
    }

    private static DocumentBuilder createDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document createDocument() {
        return createDocumentBuilder().newDocument();
    }

    public static Document createDocument(String xml) throws IOException, SAXException {
        return createDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    public static Document loadDocument(File file) throws IOException, SAXException {
        return createDocumentBuilder().parse(file);
    }

    public static Document loadDocument(InputStream is) throws IOException, SAXException {
        return createDocumentBuilder().parse(is);
    }

    public static void writeDocument(Document doc, OutputStream os) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void saveDocument(Document doc, File file) throws IOException {
        File parentDir = file.getParentFile();
        if ((parentDir != null) && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        writeDocument(doc, new FileOutputStream(file));
    }

    public static List<String> readItems(Element parent) {
        List<Element> itemElements = getChildElements(ITEM_ELEMENT, parent);
        return itemElements.stream()
                .map(Node::getTextContent)
                .collect(Collectors.toList());
    }

    public static void writeItems(Collection<String> items, Element parent) {
        items.forEach(item -> createChildElement(ITEM_ELEMENT, parent).setTextContent(item));
    }

    public static List<List<String>> readItemLists(Element parent) {
        List<Element> listElements = getChildElements(LIST_ELEMENT, parent);
        return listElements.stream()
                .map(XmlUtils::readList)
                .map(XmlUtils::readItems)
                .collect(Collectors.toList());
    }

    public static void writeItemLists(Collection<? extends Collection<String>> listOfItems, Element parent) {
        listOfItems.forEach(list -> writeItems(list, createChildElement(LIST_ELEMENT, parent)));
    }
                .collect(Collectors.toList());
    }

    public static void writeListOfLists(Collection<? extends Collection<String>> listOfItems, Element parent) {
        listOfItems.forEach(list -> writeList(list, createChildElement(LIST_ELEMENT, parent)));
    }

}
