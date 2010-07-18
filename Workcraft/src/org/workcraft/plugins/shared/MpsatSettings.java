/**
 *
 */
package org.workcraft.plugins.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.workcraft.util.FileUtils;
import org.workcraft.util.XmlUtil;

public class MpsatSettings {
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

	public MpsatSettings(MpsatMode mode, int verbosity, int satSolver,
			SolutionMode solutionMode, int solutionNumberLimit, String reach) {
		super();
		this.mode = mode;
		this.verbosity = verbosity;
		this.satSolver = satSolver;
		this.solutionMode = solutionMode;
		this.solutionNumberLimit = solutionNumberLimit;
		this.reach = reach;
	}

	public MpsatSettings(Element element) {
		mode = MpsatMode.getMode(element.getAttribute("mode"));
		verbosity = XmlUtil.readIntAttr(element, "verbosity", 0);
		solutionNumberLimit = XmlUtil.readIntAttr(element, "solutionNumberLimit", -1);
		satSolver = XmlUtil.readIntAttr(element, "satSolver", 0);
		solutionMode = SolutionMode.valueOf(XmlUtil.readStringAttr(element, "solutionMode"));

		Element re = XmlUtil.getChildElement("reach", element);
		reach = re.getTextContent();
	}

	public void toXML(Element parent) {
		Element e = parent.getOwnerDocument().createElement("settings");
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

	public SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public int getSolutionNumberLimit() {
		return solutionNumberLimit;
	}

	public String[] getMpsatArguments() {
		ArrayList<String> args = new ArrayList<String>();
		args.add(getMode().getArgument());

		if (getMode().isReach())
			try {
				File reach = File.createTempFile("reach", null);
				reach.deleteOnExit();
				FileUtils.dumpString(reach, getReach());
				args.add("-d");
				args.add("@"+reach.getCanonicalPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		args.add(String.format("-v%d", getVerbosity()));
		args.add(String.format("-$%d", getSatSolver()));

		switch (getSolutionMode()) {
		case FIRST:
			break;
		case MINIMUM_COST:
			args.add("-f");
			break;
		case ALL:
			int solutionNumberLimit = getSolutionNumberLimit();
			if (solutionNumberLimit>0)
				args.add("-a" + Integer.toString(solutionNumberLimit));
			else
				args.add("-a");
		}

		return args.toArray(new String[args.size()]);
	}
}