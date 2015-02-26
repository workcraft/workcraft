package org.workcraft.plugins.mpsat;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;

public class MpsatResultParser {
	private String mpsatOutput;
	private LinkedList<Solution> solutions;
	final static private Pattern pattern1 = Pattern.compile("SOLUTION .+\n([dDoOiI]\\..+)\npath cost:", Pattern.UNIX_LINES);
	final static private Pattern pattern2 = Pattern.compile("SOLUTION .+\n([dDoOiI]\\..+)\n([dDoOiI]\\..+)\ntotal cost of all paths:", Pattern.UNIX_LINES);

	public MpsatResultParser(ExternalProcessResult result) {
		try {
			mpsatOutput = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		solutions = new LinkedList<Solution>();
		Matcher matcher1 = pattern1.matcher(mpsatOutput);
		while (matcher1.find()) {
			Trace mainTrace = getTrace(matcher1.group(1));
			Solution solution = new Solution(mainTrace, null);
			solutions.add(solution);
		}
		Matcher matcher2 = pattern2.matcher(mpsatOutput);
		while (matcher2.find()) {
			Trace mainTrace = getTrace(matcher2.group(1));
			Trace branchTrace = getTrace(matcher2.group(2));
			Solution solution = new Solution(mainTrace, branchTrace);
			solutions.add(solution);
		}
	}

	private Trace getTrace(String mpsatTrace) {
		Trace trace = null;
		if (mpsatTrace != null) {
			trace = new Trace();
            String[] mpsatFlatTransitions = mpsatTrace.replaceAll("\\s","").split(",");
            for (String mpsatFlatTransition: mpsatFlatTransitions) {
                String mpsatTransition = mpsatFlatTransition.replace(NamespaceHelper.flatNameSeparator, NamespaceHelper.hierarchySeparator);
                String transition = mpsatTransition.substring(mpsatTransition.indexOf('.') + 1);
                trace.add(transition);
            }
		}
		return trace;
	}

	public List<Solution> getSolutions() {
		return Collections.unmodifiableList(solutions);
	}
}
