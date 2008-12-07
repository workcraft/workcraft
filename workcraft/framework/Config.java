package org.workcraft.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {

	protected HashMap<String, HashMap<String, String>> groups = new HashMap<String, HashMap<String, String>>();
	protected HashMap<String, String> rootGroup = new HashMap<String, String>();

	public String get(String key) {
		String[] k  = key.split("\\.", 2);
		HashMap<String, String> group;

		if (k.length == 1)
			return this.rootGroup.get(k[0]);
		else {
			group = this.groups.get(k[0]);
			if (group == null)
				return null;
			return group.get(k[1]);
		}
	}

	public void set(String key, String value) {
		String[] k = key.split("\\.", 2);

		HashMap<String, String> group;

		if (k.length == 1)
			this.rootGroup.put(k[0], value);
		else {
			group = this.groups.get(k[0]);
			if (group == null) {
				group = new HashMap<String, String>();
				this.groups.put(k[0], group);
			}
			group.put(k[1], value);
		}
	}

	public void load(String fileName) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document xmldoc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			xmldoc = db.parse(new File(fileName));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println ("! Warning: cannot open config file "+fileName);
			return;
		} catch (SAXException e) {
			e.printStackTrace();
			return;
		}

		Element xmlroot = xmldoc.getDocumentElement();

		NodeList nl =  xmlroot.getChildNodes(), nl2;

		for (int i=0; i<nl.getLength(); i++) {
			if (! (nl.item(i) instanceof Element))
				continue;
			Element e = (Element)nl.item(i);

			if (e.getTagName().equals("var"))
				set(e.getAttribute("name"),  e.getAttribute("value"));
			else
				if (e.getTagName().equals("group")) {
					String name = e.getAttribute("name");
					nl2 = e.getChildNodes();
					for (int j=0; j<nl2.getLength(); j++) {
						if (! (nl2.item(j) instanceof Element))
							continue;

						Element e2 = (Element)nl2.item(j);
						if (e2.getTagName().equals("var"))
							set(name+"."+e2.getAttribute("name"),  e2.getAttribute("value"));
					}
				}
		}
	}

	public void save(String fileName) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document xmldoc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			xmldoc = db.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}

		Element xmlroot = xmldoc.createElement("workcraft-config"), var, group;
		xmldoc.appendChild(xmlroot);

		for (String k : this.rootGroup.keySet()) {
			var = xmldoc.createElement("var");
			var.setAttribute("name", k);
			var.setAttribute("value", this.rootGroup.get(k));
			xmlroot.appendChild(var);
		}

		for (String k : this.groups.keySet()) {
			group = xmldoc.createElement("group");
			group.setAttribute("name", k);

			HashMap<String, String> g = this.groups.get(k);

			for (String l : g.keySet()) {
				var = xmldoc.createElement("var");
				var.setAttribute("name", l);
				var.setAttribute("value", g.get(l));
				group.appendChild(var);
			}
			xmlroot.appendChild(group);
		}

		try
		{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");



			File file = new File(fileName);

			File parentDir = file.getParentFile();
			if (parentDir != null)
				if (!parentDir.exists())
					parentDir.mkdirs();

			file.createNewFile();

			FileOutputStream fos = new FileOutputStream(file);

			DOMSource source = new DOMSource(xmldoc);
			StreamResult result = new StreamResult(new OutputStreamWriter(fos, "utf-8"));


			transformer.transform(source, result);
			fos.close();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}