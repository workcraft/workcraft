package org.workcraft.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlUtil {

	public static void saveDocument(Document doc, String path) throws IOException {
		try
		{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
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
		element.setAttribute(attributeName, value);
	}
}
