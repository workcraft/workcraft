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
	final static private Pattern patternReachability0 = Pattern.compile("SOLUTION .+\ntotal cost of all paths:", Pattern.UNIX_LINES);
	final static private Pattern patternReachability1 = Pattern.compile("SOLUTION .+\n(.+)\npath cost:", Pattern.UNIX_LINES);
	final static private Pattern patternReachability2 = Pattern.compile("SOLUTION .+\n(.+)\n(.+)\ntotal cost of all paths:", Pattern.UNIX_LINES);
	final static private Pattern patternNormalcy1 = Pattern.compile("SOLUTION .+\n(.+)\ntriggers:", Pattern.UNIX_LINES);

	public MpsatResultParser(ExternalProcessResult result) {
		try {
			mpsatOutput = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		solutions = new LinkedList<Solution>();
		Matcher matcherReachability0 = patternReachability0.matcher(mpsatOutput);
		while (matcherReachability0.find()) {
			Solution solution = new Solution(null, null);
			solutions.add(solution);
		}
		Matcher matcherReachability1 = patternReachability1.matcher(mpsatOutput);
		while (matcherReachability1.find()) {
			Trace trace = getTrace(matcherReachability1.group(1));
			Solution solution = new Solution(trace, null);
			solutions.add(solution);
		}
		Matcher matcherRreachability2 = patternReachability2.matcher(mpsatOutput);
		while (matcherRreachability2.find()) {
			Trace mainTrace = getTrace(matcherRreachability2.group(1));
			Trace branchTrace = getTrace(matcherRreachability2.group(2));
			Solution solution = new Solution(mainTrace, branchTrace);
			solutions.add(solution);
		}
		Matcher matcherNormalcy = patternNormalcy1.matcher(mpsatOutput);
		while (matcherNormalcy.find()) {
			Trace trace = getTrace(matcherNormalcy.group(1));
			Solution solution = new Solution(trace, null);
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
