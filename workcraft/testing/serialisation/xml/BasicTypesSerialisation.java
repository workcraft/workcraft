package org.workcraft.testing.serialisation.xml;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.util.XmlUtil;

public class BasicTypesSerialisation {
	public enum TestEnum {
		ONE,
		TWO,
	}

	@Test
	public void EnumTest() {
		try {
			Document doc = XmlUtil.createDocument();
			EnumSerialiser s = new EnumSerialiser();
			EnumDeserialiser ds = new EnumDeserialiser();

			Element e = doc.createElement("property");
			s.serialise(e, TestEnum.ONE);

			Element e2 = doc.createElement("property");
			s.serialise(e2, TestEnum.TWO);

			Assert.assertEquals(TestEnum.ONE, ds.deserialise(e));
			Assert.assertEquals(TestEnum.TWO, ds.deserialise(e2));


		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SerialisationException e) {
			e.printStackTrace();
		} catch (DeserialisationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void IntTest() {
		try {
			Document doc = XmlUtil.createDocument();
			IntSerialiser s = new IntSerialiser();
			IntDeserialiser ds = new IntDeserialiser();

			Element e = doc.createElement("property");
			s.serialise(e, 8);

			Element e2 = doc.createElement("property");
			s.serialise(e2,-500);

			Assert.assertEquals(8, ds.deserialise(e));
			Assert.assertEquals(-500, ds.deserialise(e2));


		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SerialisationException e) {
			e.printStackTrace();
		} catch (DeserialisationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void DoubleTest() {
		try {
			Document doc = XmlUtil.createDocument();
			DoubleSerialiser s = new DoubleSerialiser();
			DoubleDeserialiser ds = new DoubleDeserialiser();

			Element e = doc.createElement("property");
			s.serialise(e, -1E8);

			Element e2 = doc.createElement("property");
			s.serialise(e2, 123.456);

			Assert.assertEquals(-1E8, ds.deserialise(e));
			Assert.assertEquals(123.456, ds.deserialise(e2));


		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SerialisationException e) {
			e.printStackTrace();
		} catch (DeserialisationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void StringTest() {
		try {
			Document doc = XmlUtil.createDocument();
			StringSerialiser s = new StringSerialiser();
			StringDeserialiser ds = new StringDeserialiser();

			Element root = doc.createElement("node");
			doc.appendChild(root);

			Element e1 = doc.createElement("property");
			s.serialise(e1, "череззаборногузадерищенко");

			Element e2 = doc.createElement("property");
			s.serialise(e2, "");

			Element e3 = doc.createElement("property");
			s.serialise(e3, "\" <xml");

			root.appendChild(e1);root.appendChild(e2);root.appendChild(e3);

			// XmlUtil.writeDocument(doc, System.out);

			Assert.assertEquals("череззаборногузадерищенко", ds.deserialise(e1));
			Assert.assertEquals("", ds.deserialise(e2));
			Assert.assertEquals("\" <xml", ds.deserialise(e3));

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SerialisationException e) {
			e.printStackTrace();
		} catch (DeserialisationException e) {
			e.printStackTrace();
		}
	}
}
