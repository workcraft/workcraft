package org.workcraft.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {
	public static List<Element> getChildElements (String tagName, Element element) {
			LinkedList<Element> result = new LinkedList<Element>();
			NodeList nl = element.getChildNodes();
			for (int i=0; i<nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tagName))
					result.add((Element)n);
			}
			return result;
	}

	public static Element getChildElement (String tagName, Element element) {
		NodeList nl = element.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tagName))
				return (Element)n;
		}
		return null;
	}

	public static Element createChildElement (String tagName, Element parentElement) {
		Element result = parentElement.getOwnerDocument().createElement(tagName);
		parentElement.appendChild(result);
		return result;
	}

	public static void saveDocument(Document doc, String path) throws IOException {
		try
		{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			FileOutputStream fos = new FileOutputStream(path);

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new OutputStreamWriter(fos));

			transformer.transform(source, result);
			fos.close();
		} catch (TransformerException e) {
			System.err.println(e.getMessage());
		}
	}

	public static int readIntAttr (Element element, String attributeName, int defaultValue)  {
		String attributeValue = element.getAttribute(attributeName);
		try {
			return Integer.parseInt(attributeValue);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static void writeIntAttr (Element element, String attributeName, int value) {
		element.setAttribute(attributeName, Integer.toString(value));
	}

	public static double readDoubleAttr (Element element, String attributeName, double defaultValue)  {
		String attributeValue = element.getAttribute(attributeName);
		try {
			return Double.parseDouble(attributeValue);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static void writeDoubleAttr (Element element, String attributeName, double value) {
		element.setAttribute(attributeName, Double.toString(value));
	}

	public static boolean readBoolAttr (Element element, String attributeName)  {
		String attributeValue = element.getAttribute(attributeName);
		return Boolean.parseBoolean(attributeValue);
	}

	public static void writeBoolAttr (Element element, String attributeName, boolean value) {
		element.setAttribute(attributeName, Boolean.toString(value));
	}

	public static String readStringAttr (Element element, String attributeName)
	{
		return element.getAttribute(attributeName);
	}

	public static void writeStringAttr (Element element, String attributeName, String value) {
		element.setAttribute(attributeName, (value==null)?"":value);
	}
}
