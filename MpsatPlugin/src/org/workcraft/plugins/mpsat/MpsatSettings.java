/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.util.FileUtils;

public class MpsatSettings {
	public static final int SOLVER_ZCHAFF = 0;
	public static final int SOLVER_MINISAT = 1;

	public enum SolutionMode {
		MINIMUM_COST("Minimal cost"),
		FIRST("First"),
		ALL("First 10 solutions");

		public final String name;

		private SolutionMode(String name) {
			this.name = name;
		}

		static public Map<String, SolutionMode> getChoice() {
			LinkedHashMap<String, SolutionMode> choice = new LinkedHashMap<String, SolutionMode>();
			for (SolutionMode item : SolutionMode.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	}

	private MpsatMode mode = MpsatMode.DEADLOCK;
	private int verbosity = 0;
	private int satSolver = 0;
	private SolutionMode solutionMode = SolutionMode.FIRST;
	private int solutionNumberLimit = 0;
	private String reach = "";

	// setup for searching semimodularity
	public static final String propertySemimodularity =
		"card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n"+
		"	exists t1 in tran EVENTS s.t. sig t1 in LOCAL {\n"+
		"	  @t1 &\n"+
		"	  exists t2 in tran EVENTS s.t. sig t2 != sig t1 & card (pre t1 * (pre t2 \\ post t2)) != 0 {\n"+
		"	    @t2 &\n"+
		"	    forall t3 in tran EVENTS * (tran sig t1 \\ {t1}) s.t. card (pre t3 * (pre t2 \\ post t2)) = 0 {\n"+
		"	       exists p in pre t3 \\ post t2 { ~$p }\n"+
		"	    }\n"+
		"	  }\n"+
		"	}\n";


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