/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
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

	private static final Pattern nodeNamePattern = Pattern.compile("\"\\s*(.+?)\\s*\"");

	private final String name;
	private final MpsatMode mode;
	private final int verbosity;
	private final SolutionMode solutionMode;
	private final int solutionNumberLimit;
	private final String reach;

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

	// Reach expression for checking conformation (this is a template,
	// the list of places needs to be updated for each circuit)
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

	// FIXME: Currently Punf does not support dead signals, transitions and places well
	//        (e.g. a dead transition may disappear from unfolding), therefore the
	//        conformation property cannot be checked reliably.
	public static String genReachConformation(STG stg, STG circuitStg) {
		// Form a set of system STG places which came from the circuitStg
		HashSet<Node> circuitPlaces = new HashSet<Node>();
		for (Type type: Type.values()) {
			for (String s : circuitStg.getSignalReferences(type)) {
				Node p0 = stg.getNodeByReference(s + "_0");
				if (p0 == null) {
					p0 = stg.getNodeByReference("<" + s + "-," + s + "+>");
				}
				if (p0 != null) {
					circuitPlaces.add(p0);
				}
				Node p1 = stg.getNodeByReference(s + "_1");
				if (p1 == null) {
					p1 = stg.getNodeByReference("<" + s + "+," + s + "->");
				}
				if (p1 != null) {
					circuitPlaces.add(p1);
				}
			}
		}
		// Generate Reach expression
		String result = "";
		for (String s : circuitStg.getSignalReferences(Type.OUTPUT)) {
			String circuitPredicate = "";
			String environmentPredicate = "";
			for (SignalTransition t: stg.getSignalTransitions()) {
				if (!t.getSignalType().equals(Type.OUTPUT) || !t.getSignalName().equals(s)) continue;
				String circuitPreset = "";
				String environmentPreset = "";
				for (Node p: stg.getPreset(t)) {
					String name = stg.getNodeReference(p);
					if (circuitPlaces.contains(p)) {
						if (circuitPreset.isEmpty()) {
							circuitPreset += "{";
						} else {
							circuitPreset += ", ";
						}
						circuitPreset += "\"" + name + "\"";
					} else {
						if (environmentPreset.isEmpty()) {
							environmentPreset += "{";
						} else {
							environmentPreset += ", ";
						}
						environmentPreset += "\"" + name + "\"";
					}
				}
				circuitPreset += "}";
				environmentPreset += "}";

				if (circuitPredicate.isEmpty()) {
					circuitPredicate += "   (\n";
				} else {
					circuitPredicate += "      |\n";
				}
				circuitPredicate += "      forall p in " + circuitPreset  + " {\n";
				circuitPredicate += "         $ P p\n";
				circuitPredicate += "      }\n";

				if (environmentPredicate.isEmpty()) {
					environmentPredicate += "   (\n";
				} else {
					environmentPredicate += "      &\n";
				}
				environmentPredicate += "      exists p in " + environmentPreset  + " {\n";
				environmentPredicate += "         ~$ P p\n";
				environmentPredicate += "      }\n";
			}
			circuitPredicate += "   )\n";
			environmentPredicate += "   )\n";
			if (!result.isEmpty()) {
				result += "|\n";
			}
			result += "/* Conformation check for signal " + s + " */\n";
			result += "(\n";
			result += circuitPredicate;
			result += "   &\n";
			result += environmentPredicate;
			result += ")\n";
		}
		return result;
	}


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

	private String getFlatReach() {
		StringBuffer sb = new StringBuffer(reach.length());
		Matcher matcher = nodeNamePattern.matcher(reach);
		while (matcher.find()) {
			String reference = matcher.group(1);
			String flatName = NamespaceHelper.getFlatName(reference);
			matcher.appendReplacement(sb, "\"" + flatName + "\"");
		}
		matcher.appendTail(sb);
		return sb.toString();
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

				FileUtils.dumpString(reach, getFlatReach());
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
			if (solutionNumberLimit>0) {
				args.add("-a" + Integer.toString(solutionNumberLimit));
			} else {
				args.add("-a");
			}
		}

		return args.toArray(new String[args.size()]);
	}
}