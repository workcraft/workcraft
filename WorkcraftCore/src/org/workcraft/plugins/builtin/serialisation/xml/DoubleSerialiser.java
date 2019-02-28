package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.BasicXMLSerialiser;

public class DoubleSerialiser implements BasicXMLSerialiser<Double> {

    public static String doubleToString(double d) {
        return Double.toHexString(d);
    }

    public static String doubleToString(Double d) {
        return doubleToString(d.doubleValue());
    }

    public static double doubleFromString(String s) {
        return Double.parseDouble(s);
    }

    @Override
    public String getClassName() {
        return double.class.getName();
    }

    @Override
    public void serialise(Element element, Double object) throws SerialisationException {
        element.setAttribute("value", doubleToString(object));
    }

}
