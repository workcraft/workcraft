package org.workcraft.plugins.wtg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class GuardSerialiser implements BasicXMLSerialiser<Guard> {

    @Override
    public String getClassName() {
        return Guard.class.getName();
    }

    @Override
    public void serialise(Element element, Guard object) {
        element.setAttribute("guard", object.toString());
    }

}
