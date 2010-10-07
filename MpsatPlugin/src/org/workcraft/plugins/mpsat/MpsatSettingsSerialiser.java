package org.workcraft.plugins.mpsat;

import org.w3c.dom.Element;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;
import org.workcraft.util.XmlUtil;

public class MpsatSettingsSerialiser implements SettingsSerialiser<MpsatSettings> {

	public MpsatSettings fromXML (Element element) {
		MpsatMode mode = MpsatMode.getMode(element.getAttribute("mode"));
		int verbosity = XmlUtil.readIntAttr(element, "verbosity", 0);
		int solutionNumberLimit = XmlUtil.readIntAttr(element, "solutionNumberLimit", -1);
		int satSolver = XmlUtil.readIntAttr(element, "satSolver", 0);
		SolutionMode solutionMode = SolutionMode.valueOf(XmlUtil.readStringAttr(element, "solutionMode"));

		Element re = XmlUtil.getChildElement("reach", element);
		String reach = re.getTextContent();

		return new MpsatSettings(mode, verbosity, satSolver, solutionMode, solutionNumberLimit, reach);
	}

	public void toXML(MpsatSettings settings, Element parent) {
		Element e = parent.getOwnerDocument().createElement("settings");
		e.setAttribute("mode", settings.getMode().getArgument());
		e.setAttribute("verbosity", Integer.toString(settings.getVerbosity()));
		e.setAttribute("satSolver", Integer.toString(settings.getSatSolver()));
		e.setAttribute("solutionMode", settings.getSolutionMode().name());
		e.setAttribute("solutionNumberLimit", Integer.toString(settings.getSolutionNumberLimit()));

		Element re = parent.getOwnerDocument().createElement("reach");
		re.setTextContent(settings.getReach());

		e.appendChild(re);
		parent.appendChild(e);
	}
}
