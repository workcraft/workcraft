package org.workcraft.plugins.verification;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;

public class MpsatResultParser {
	private String mpsatOutput;
	private LinkedList<Trace> solutions;

	public MpsatResultParser(ExternalProcessResult result) {
		try {
			mpsatOutput = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		solutions = new LinkedList<Trace>();

		Pattern solution = Pattern.compile("SOLUTION.*\n(.*?)\n", Pattern.UNIX_LINES);
		Matcher m = solution.matcher(mpsatOutput);

		while (m.find()) {
			Trace trace = new Trace();

			String mpsatTrace = m.group(1);
			if (!mpsatTrace.isEmpty()) {
				String[] ss = mpsatTrace.split(",");

				for (String k: ss) {
					if (k.charAt(1) == '.')
						trace.add(k.substring(2).trim());
					else
						trace.add(k.trim());
				}
			}

			solutions.add(trace);
		}
	}

	public List<Trace> getSolutions() {
		return Collections.unmodifiableList(solutions);
	}
}
