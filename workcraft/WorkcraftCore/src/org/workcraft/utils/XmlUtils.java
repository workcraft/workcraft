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
import java.util.LinkedList;
import java.util.List;

public class XmlUtils {

    public static String readTextAttribute(Element element, String attributeName, String defaultValue) {
        if ((element == null) || !element.hasAttribute(attributeName)) {
            return defaultValue;
        }
        return element.getAttribute(attributeName);
    }

    public static int readIntAttribute(Element element, String attributeName, int defaultValue) {
        if ((element == null) || !element.hasAttribute(attributeName)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(element.getAttribute(attributeName));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean readBooleanAttribute(Element element, String attributeName,
            boolean defaultValue) {

        if ((element == null) || !element.hasAttribute(attributeName)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(element.getAttribute(attributeName));
    }

    public static <T extends Enum<T>> T readEnumAttribute(Element element, String attributeName,
            Class<T> enumType, T defaultValue) {

        if ((element == null) || !element.hasAttribute(attributeName)) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, element.getAttribute(attributeName));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
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

}
