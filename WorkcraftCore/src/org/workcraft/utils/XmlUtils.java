package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;

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

    public static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
    }

    public static Document loadDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(file);
    }

    public static Document loadDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(is);
    }

    public static void writeDocument(Document doc, OutputStream os) {
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

}
