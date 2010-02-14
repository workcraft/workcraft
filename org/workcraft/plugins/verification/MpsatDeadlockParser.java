package org.workcraft.plugins.verification;

import org.workcraft.Trace;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;

public class MpsatDeadlockParser extends MpsatResultParser {
	public MpsatDeadlockParser(ExternalProcessResult result) {
		super(result);

		/* Pattern pat = Pattern.compile("SOLUTION.*\n(.*?)\n", Pattern.UNIX_LINES);
		Matcher m = pat.matcher(mpsatOutput);

		if (m.find()) {
			String mpsat_trace = m.group(1);
			String[] ss = mpsat_trace.split(",");
			String trace = "";

			for (String k: ss) {
				if (trace.length()>0)
					trace+=";";
				if (k.startsWith("d."))
					trace += k.substring(2).trim();
				else
					trace += k.trim();
			}

			return trace;
		} else
			return null; */
	}

	public boolean hasDeadlock() {
		return true;
	}

	public Trace[] getViolationTraces() {
		return null;
	}
}