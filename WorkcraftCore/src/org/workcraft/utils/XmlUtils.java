package org.workcraft.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtils {

    public static List<Element> getChildElements(String tagName, Element element) {
        LinkedList<Element> result = new LinkedList<>();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if ((n.getNodeType() == Node.ELEMENT_NODE) && tagName.equals(n.getNodeName())) {
                result.add((Element) n);
            }
        }
        return result;
    }

    public static Element getChildElement(String tagName, Element element) {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if ((n.getNodeType() == Node.ELEMENT_NODE) && tagName.equals(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    public static Element createChildElement(String tagName, Element parentElement) {
        Element result = parentElement.getOwnerDocument().createElement(tagName);
        parentElement.appendChild(result);
        return result;
    }

    public static void writeDocument(Document doc, OutputStream os) throws IOException {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
            StreamResult result = new StreamResult(new OutputStreamWriter(os, utf8Encoder));
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

    public static void saveDocument(Document doc, File transform, File file) throws IOException {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setAttribute("indent-number", Integer.valueOf(2));
            Transformer transformer = tFactory.newTransformer(new StreamSource(transform));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            FileOutputStream fos = new FileOutputStream(file.getPath());

            DOMSource source = new DOMSource(doc);
            CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
            StreamResult result = new StreamResult(new OutputStreamWriter(fos, utf8Encoder));

            transformer.transform(source, result);
            fos.close();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static void writeColorAttr(Element element, String attributeName, Color value) {
        element.setAttribute(attributeName, String.format("#%x", value.getRGB() & 0xffffff));
    }

    public static Color readColorAttr(Element element, String attributeName, Color defaultValue) {
        String s = element.getAttribute(attributeName);

        if (s == null || s.charAt(0) != '#') {
            return defaultValue;
        }

        try {
            return new Color(Integer.parseInt(s.substring(1), 16), false);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static int readIntAttr(Element element, String attributeName, int defaultValue) {
        String attributeValue = element.getAttribute(attributeName);
        try {
            return Integer.parseInt(attributeValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void writeIntAttr(Element element, String attributeName, int value) {
        element.setAttribute(attributeName, Integer.toString(value));
    }

    public static double readDoubleAttr(Element element, String attributeName, double defaultValue) {
        String attributeValue = element.getAttribute(attributeName);
        try {
            return Double.parseDouble(attributeValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void writeDoubleAttr(Element element, String attributeName, double value) {
        element.setAttribute(attributeName, Double.toString(value));
    }

    public static boolean readBoolAttr(Element element, String attributeName) {
        String attributeValue = element.getAttribute(attributeName);
        return Boolean.parseBoolean(attributeValue);
    }

    public static void writeBoolAttr(Element element, String attributeName, boolean value) {
        element.setAttribute(attributeName, Boolean.toString(value));
    }

    public static String readStringAttr(Element element, String attributeName) {
        return element.getAttribute(attributeName);
    }

    public static void writeStringAttr(Element element, String attributeName, String value) {
        element.setAttribute(attributeName, (value == null) ? "" : value);
    }

    public static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();
        doc = db.newDocument();
        return doc;
    }

    public static Document loadDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();
        doc = db.parse(file);

        return doc;
    }

    public static Document loadDocument(String path) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();
        doc = db.parse(new File(path));

        return doc;
    }

    public static Document loadDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();
        doc = db.parse(is);

        return doc;
    }

    public static Document loadDocument(ReadableByteChannel in) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();
        doc = db.parse(Channels.newInputStream(in));

        return doc;
    }

}
