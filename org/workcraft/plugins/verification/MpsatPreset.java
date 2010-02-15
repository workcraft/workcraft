/**
 *
 */
package org.workcraft.plugins.verification;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class MpsatPreset {
	public static final int SOLVER_ZCHAFF = 0;
	public static final int SOLVER_MINISAT = 1;

	public enum SolutionMode {
		FIRST,
		ALL,
		MINIMUM_COST
	}

	private MpsatMode mode;
	private int verbosity;
	private int satSolver;
	private SolutionMode solutionMode;
	private int solutionNumberLimit;
	private String reach;
	private String description;
	private boolean builtIn;

	public MpsatPreset(MpsatMode mode, int verbosity, int satSolver,
			SolutionMode solutionMode, int solutionNumberLimit, String reach, String description, boolean builtIn) {
		super();
		this.mode = mode;
		this.verbosity = verbosity;
		this.satSolver = satSolver;
		this.solutionMode = solutionMode;
		this.solutionNumberLimit = solutionNumberLimit;
		this.reach = reach;
		this.description = description;
		this.builtIn = builtIn;
	}

	public MpsatPreset(Element element) {
		description = element.getAttribute("description");
		mode = MpsatMode.getMode(element.getAttribute("mode"));
		verbosity = XmlUtil.readIntAttr(element, "verbosity", 0);
		solutionNumberLimit = XmlUtil.readIntAttr(element, "solutionNumberLimit", -1);
		satSolver = XmlUtil.readIntAttr(element, "satSolver", 0);
		solutionMode = SolutionMode.valueOf(XmlUtil.readStringAttr(element, "solutionMode"));

		Element re = XmlUtil.getChildElement("reach", element);
		reach = re.getTextContent();
	}

	public void toXML(Element parent) {
		Element e = parent.getOwnerDocument().createElement("preset");
		e.setAttribute("description", description);
		e.setAttribute("mode", mode.getArgument());
		e.setAttribute("verbosity", Integer.toString(verbosity));
		e.setAttribute("satSolver", Integer.toString(satSolver));
		e.setAttribute("solutionMode", solutionMode.name());
		e.setAttribute("solutionNumberLimit", Integer.toString(solutionNumberLimit));

		Element re = parent.getOwnerDocument().createElement("reach");
		re.setTextContent(reach);

		e.appendChild(re);
		parent.appendChild(e);
	}

	public String toString() {
		return builtIn?description + " [built-in]":description;
	}

	public MpsatMode getMode() {
		return mode;
	}

	public int getVerbosity() {
		return verbosity;
	}

	public int getSatSolver() {
		return satSolver;
	}

	public String getReach() {
		return reach;
	}

	public String getDescription() {
		return description;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	public SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public int getSolutionNumberLimit() {
		return solutionNumberLimit;
	}
}