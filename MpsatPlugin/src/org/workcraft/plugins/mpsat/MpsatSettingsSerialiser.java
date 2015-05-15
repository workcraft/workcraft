package org.workcraft.plugins.mpsat;

import org.w3c.dom.Element;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;
import org.workcraft.util.XmlUtil;

public class MpsatSettingsSerialiser implements SettingsSerialiser<MpsatSettings> {

	public MpsatSettings fromXML (Element element) {
		String name = XmlUtil.readStringAttr(element, "name");
		MpsatMode mode = MpsatMode.getModeByArgument(element.getAttribute("mode"));
		int verbosity = XmlUtil.readIntAttr(element, "verbosity", 0);
		int solutionNumberLimit = XmlUtil.readIntAttr(element, "solutionNumberLimit", -1);
		SolutionMode solutionMode = SolutionMode.valueOf(XmlUtil.readStringAttr(element, "solutionMode"));

		Element re = XmlUtil.getChildElement("reach", element);
		String reach = re.getTextContent();
		boolean inversePredicate = XmlUtil.readBoolAttr(element, "inversePredicate");

		return new MpsatSettings(name, mode, verbosity, solutionMode, solutionNumberLimit, reach, inversePredicate);
	}

	public void toXML(MpsatSettings settings, Element parent) {
		Element e = parent.getOwnerDocument().createElement("settings");
		e.setAttribute("name", settings.getName());
		e.setAttribute("mode", settings.getMode().getArgument());
		e.setAttribute("verbosity", Integer.toString(settings.getVerbosity()));
		e.setAttribute("solutionMode", settings.getSolutionMode().name());
		e.setAttribute("solutionNumberLimit", Integer.toString(settings.getSolutionNumberLimit()));

		Element reach = parent.getOwnerDocument().createElement("reach");
		reach.setTextContent(settings.getReach());
		e.appendChild(reach);

		e.setAttribute("inversePredicate", Boolean.toString(settings.getInversePredicate()));

		parent.appendChild(e);
	}
}
