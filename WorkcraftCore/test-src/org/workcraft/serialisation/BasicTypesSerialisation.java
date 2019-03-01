package org.workcraft.serialisation;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.serialisation.xml.*;
import org.workcraft.utils.XmlUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.AffineTransform;

public class BasicTypesSerialisation {

    public enum TestEnum {
        ONE,
        TWO,
    }

    @Test
    public void enumTest() {
        try {
            Document doc = XmlUtils.createDocument();
            EnumSerialiser s = new EnumSerialiser();
            EnumDeserialiser ds = new EnumDeserialiser();

            Element e = doc.createElement("property");
            s.serialise(e, TestEnum.ONE);

            Element e2 = doc.createElement("property");
            s.serialise(e2, TestEnum.TWO);

            Assert.assertEquals(TestEnum.ONE, ds.deserialise(e));
            Assert.assertEquals(TestEnum.TWO, ds.deserialise(e2));
        } catch (ParserConfigurationException | SerialisationException | DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void intTest() {
        try {
            Document doc = XmlUtils.createDocument();
            IntSerialiser s = new IntSerialiser();
            IntDeserialiser ds = new IntDeserialiser();

            Element e = doc.createElement("property");
            s.serialise(e, 8);

            Element e2 = doc.createElement("property");
            s.serialise(e2, -500);

            Assert.assertEquals(Integer.valueOf(8), ds.deserialise(e));
            Assert.assertEquals(Integer.valueOf(-500), ds.deserialise(e2));
        } catch (ParserConfigurationException | SerialisationException  e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void doubleTest() {
        try {
            Document doc = XmlUtils.createDocument();
            DoubleSerialiser s = new DoubleSerialiser();
            DoubleDeserialiser ds = new DoubleDeserialiser();

            Element e = doc.createElement("property");
            s.serialise(e, -1E8);

            Element e2 = doc.createElement("property");
            s.serialise(e2, 123.456);

            double r = Math.random();
            Element e3 = doc.createElement("property");
            s.serialise(e3, r);

            Assert.assertEquals(Double.valueOf(-1E8), ds.deserialise(e));
            Assert.assertEquals(Double.valueOf(123.456), ds.deserialise(e2));
            Assert.assertEquals(Double.valueOf(r), ds.deserialise(e3));
        } catch (ParserConfigurationException | SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void transformTest() {
        try {
            Document doc = XmlUtils.createDocument();
            AffineTransformSerialiser s = new AffineTransformSerialiser();
            AffineTransformDeserialiser ds = new AffineTransformDeserialiser();

            AffineTransform t = new AffineTransform(
                    Math.random(), Math.random(), Math.random(),
                    Math.random(), Math.random(), Math.random()
                );
            Element e = doc.createElement("property");
            s.serialise(e, t);

            Assert.assertEquals(t, ds.deserialise(e));
        } catch (ParserConfigurationException | SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void stringTest() {
        try {
            Document doc = XmlUtils.createDocument();
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

            root.appendChild(e1);
            root.appendChild(e2);
            root.appendChild(e3);

            // XmlUtil.writeDocument(doc, System.out);

            Assert.assertEquals("череззаборногузадерищенко", ds.deserialise(e1));
            Assert.assertEquals("", ds.deserialise(e2));
            Assert.assertEquals("\" <xml", ds.deserialise(e3));
        } catch (ParserConfigurationException | SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

}
