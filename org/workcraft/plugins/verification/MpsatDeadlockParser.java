package org.workcraft.plugins.verification;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;

public class MpsatDeadlockParser extends MpsatResultParser {
	private ArrayList<Trace> solutions = new ArrayList<Trace>();

	public MpsatDeadlockParser(ExternalProcessResult result) {
		super(result);

		String output;

		try {
			output = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		Pattern solution = Pattern.compile("SOLUTION.*\n(.*?)\n", Pattern.UNIX_LINES);
		Matcher m = solution.matcher(output);

		while (m.find()) {
			Trace trace = new Trace();

			String[] ss = m.group(1).split(",");

			for (String k: ss) {
				if (k.charAt(1) == '.')
					trace.add(k.substring(2).trim());
				else
					trace.add(k.trim());
			}

			solutions.add(trace);
		}
	}

	public boolean hasDeadlock() {
		return !solutions.isEmpty();
	}

	public List<Trace> getSolutions() {
		return solutions;
	}
}