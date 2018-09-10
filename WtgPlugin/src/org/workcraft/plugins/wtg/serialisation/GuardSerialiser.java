package org.workcraft.plugins.wtg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class GuardSerialiser implements BasicXMLSerialiser {

    public String getClassName() {
        return Guard.class.getName();
    }

    public void serialise(Element element, Object object)
            throws SerialisationException {
        Guard guard = (Guard) object;
        element.setAttribute("guard", guard.toString());
    }

}
