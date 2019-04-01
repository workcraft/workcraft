package org.workcraft.plugins.mpsat;

import org.w3c.dom.Element;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.presets.SettingsSerialiser;
import org.workcraft.utils.XmlUtils;

public class MpsatSettingsSerialiser implements SettingsSerialiser<VerificationParameters> {

    public VerificationParameters fromXML(Element element) {
        String name = XmlUtils.readStringAttr(element, "name");
        VerificationMode mode = VerificationMode.getModeByArgument(element.getAttribute("mode"));
        int verbosity = XmlUtils.readIntAttr(element, "verbosity", 0);
        int solutionNumberLimit = XmlUtils.readIntAttr(element, "solutionNumberLimit", -1);
        SolutionMode solutionMode = SolutionMode.valueOf(XmlUtils.readStringAttr(element, "solutionMode"));

        Element re = XmlUtils.getChildElement("reach", element);
        String reach = re.getTextContent();
        boolean inversePredicate = XmlUtils.readBoolAttr(element, "inversePredicate");

        return new VerificationParameters(name, mode, verbosity, solutionMode, solutionNumberLimit, reach, inversePredicate);
    }

    public void toXML(VerificationParameters settings, Element parent) {
        Element e = parent.getOwnerDocument().createElement("settings");
        e.setAttribute("name", settings.getName());
        e.setAttribute("mode", settings.getMode().getArgument());
        e.setAttribute("verbosity", Integer.toString(settings.getVerbosity()));
        e.setAttribute("solutionMode", settings.getSolutionMode().name());
        e.setAttribute("solutionNumberLimit", Integer.toString(settings.getSolutionNumberLimit()));

        Element reach = parent.getOwnerDocument().createElement("reach");
        reach.setTextContent(settings.getExpression());
        e.appendChild(reach);

        e.setAttribute("inversePredicate", Boolean.toString(settings.getInversePredicate()));

        parent.appendChild(e);
    }
}
