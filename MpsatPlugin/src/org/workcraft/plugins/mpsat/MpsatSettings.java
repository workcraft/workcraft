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

	public enum SolutionMode {
		MINIMUM_COST("Minimal cost solution"),
		FIRST("First solution"),
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

	private String name = null;
	private MpsatMode mode = MpsatMode.DEADLOCK;
	private int verbosity = 0;
	private SolutionMode solutionMode = SolutionMode.FIRST;
	private int solutionNumberLimit = 0;
	private String reach = "";

	// Reach expression for checking signal consistency
	public static final String reachConsistency =
		"exists s in SIGNALS \\ DUMMY {\n" +
		"  let Es = ev s {\n" +
		"    $s & exists e in Es s.t. is_plus e { @e }\n" +
		"    |\n" +
		"    ~$s & exists e in Es s.t. is_minus e { @e }\n" +
		"  }\n" +
		"}\n";

	// Reach expression for checking semimodularity (output persistency)
	public static final String reachSemimodularity =
		"card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n" +
		"  exists t1 in tran EVENTS s.t. sig t1 in LOCAL {\n" +
		"    @t1 &\n" +
		"    exists t2 in tran EVENTS s.t. sig t2 != sig t1 & card (pre t1 * (pre t2 \\ post t2)) != 0 {\n" +
		"      @t2 &\n" +
		"      forall t3 in tran EVENTS * (tran sig t1 \\ {t1}) s.t. card (pre t3 * (pre t2 \\ post t2)) = 0 {\n" +
		"        exists p in pre t3 \\ post t2 { ~$p }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n";

	// Reach expression for checking conformation
	public static final String reachConformation =
		"let CPnames = {\"in1_0\", \"in1_1\", \"in0_0\", \"in0_1\", \"out0_0\", \"out0_1\"},\n" +
		"CP=gather n in CPnames { P n } {\n" +
		"	exists s in SIGNALS \\ DUMMY {\n" +
		"		exists t in tran s {\n" +
		"			forall p in pre t * CP { $p }\n" +
		"		}\n" +
		"		&\n" +
		"		forall t in tran s {\n" +
		"			exists p in pre t \\ CP { ~$p }\n" +
		"		}\n" +
		"	}\n" +
		"}\n";

	public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit, String reach) {
		super();
		this.name = name;
		this.mode = mode;
		this.verbosity = verbosity;
		this.solutionMode = solutionMode;
		this.solutionNumberLimit = solutionNumberLimit;
		this.reach = reach;
	}

	public String getName() {
		return name;
	}

	public MpsatMode getMode() {
		return mode;
	}

	public int getVerbosity() {
		return verbosity;
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