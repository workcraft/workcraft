/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.FileUtils;

public class MpsatSettings {

	public enum SolutionMode {
		MINIMUM_COST("Minimal cost solution"),
		FIRST("First solution"),
		ALL("First 10 solutions");

		private final String name;

		private SolutionMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final Pattern nodeNamePattern = Pattern.compile("\"(\\S+?)\"");

	private final String name;
	private final MpsatMode mode;
	private final int verbosity;
	private final SolutionMode solutionMode;
	private final int solutionNumberLimit;
	private final String reach;
	private final String satisfiableMessage;
	private final String unsatisfiableMessage;

	// Reach expression for checking signal consistency
	public static final String reachConsistency =
		"exists s in SIGNALS \\ DUMMY {\n" +
		"    let Es = ev s {\n" +
		"        $s & exists e in Es s.t. is_plus e { @e }\n" +
		"        |\n" +
		"        ~$s & exists e in Es s.t. is_minus e { @e }\n" +
		"    }\n" +
		"}\n";

	// Reach expression for checking semimodularity (output persistency)
	public static final String reachSemimodularity =
		"card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n" +
		"    exists t1 in tran EVENTS s.t. sig t1 in LOCAL {\n" +
		"        @t1 &\n" +
		"        exists t2 in tran EVENTS s.t. sig t2 != sig t1 & card (pre t1 * (pre t2 \\ post t2)) != 0 {\n" +
		"            @t2 &\n" +
		"            forall t3 in tran EVENTS * (tran sig t1 \\ {t1}) s.t. card (pre t3 * (pre t2 \\ post t2)) = 0 {\n" +
		"                exists p in pre t3 \\ post t2 { ~$p }\n" +
		"            }\n" +
		"        }\n" +
		"    }\n";

	// Reach expression for checking conformation (this is a template,
	// the list of places needs to be updated for each circuit)
	public static final String reachConformation =
//			"let devOutputs = gather signalName in { \"out0\" } { S signalName },\n" +
//			"    devPlaces = gather placeName in { \"in1_0\", \"in1_1\", \"in0_1\", \"in0_0\", \"<out0+,out0->\", \"<out0-,out0+>\" } { P placeName } {\n" +
			"    card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n" +
			"    exists s in devOutputs {\n" +
			"        exists t in tran s {\n" +
			"            is_plus t & forall p in pre t * devPlaces { $ p }\n" +
			"        }\n" +
			"        &\n" +
			"        forall t in tran s {\n" +
			"            is_plus t & exists p in pre t \\ devPlaces { ~ $ p }\n" +
			"        }\n" +
			"        |\n" +
			"        exists t in tran s {\n" +
			"            is_minus t & forall p in pre t * devPlaces { $ p }\n" +
			"        }\n" +
			"        &\n" +
			"        forall t in tran s {\n" +
			"            is_minus t & exists p in pre t \\ devPlaces { ~ $ p }\n" +
			"        }\n" +
			"    }\n" +
			"}\n";

	// Note: New (PNML-based) version of Punf is required to check conformation property.
	// Old version of Punf does not support dead signals, transitions and places well
	// (e.g. a dead transition may disappear from unfolding), therefore the conformation
	// property cannot be checked reliably.
	public static String genReachConformation(Set<String> devOutputNames, Set<String> devPlaceNames) {
		String devOutputList = genNameList(devOutputNames);
		String devPlaceList = genNameList(devPlaceNames);
		return "let devOutputs = gather signalName in { " + devOutputList + " } { S signalName },\n" +
			   "    devPlaces = gather placeName in { " + devPlaceList + " } { P placeName } {\n" +
			   reachConformation;
	}

	private static String genNameList(Collection<String> names) {
		String result = "";
		for (String name: names) {
			if ( !result.isEmpty() ) {
				result += ", ";
			}
			result += "\"" + name + "\"";
		}
		return result;
	}

	public static String genReachConformationDetail(STG stg, Set<String> devOutputNames, Set<String> devPlaceNames) {
		String result = "";
		for (String signalFlatName: devOutputNames) {
			String riseDevPredicate = "";
			String fallDevPredicate = "";
			String riseEnvPredicate = "";
			String fallEnvPredicate = "";

			String signalRef = NamespaceHelper.flatToHierarchicalName(signalFlatName);
			for (SignalTransition t: stg.getSignalTransitions(signalRef)) {
				String devPreset = "";
				String envPreset = "";
				for (Node p: stg.getPreset(t)) {
					String placeRef = stg.getNodeReference(p);
					String placeFlatName = NamespaceHelper.hierarchicalToFlatName(placeRef);
					if (devPlaceNames.contains(placeFlatName)) {
						devPreset += (devPreset.isEmpty() ? "{" : ", ");
						devPreset += "\"" + placeFlatName + "\"";
					}
					envPreset += (envPreset.isEmpty() ? "{" : ", ");
					envPreset += "\"" + placeFlatName + "\"";
				}

				if ( !devPreset.isEmpty() && !envPreset.isEmpty() ) {
					devPreset += "}";
					envPreset += "}";
					String devPredicate = "";
					devPredicate += "        forall p in " + devPreset  + " {\n";
					devPredicate += "            $ P p\n";
					devPredicate += "        }\n";
					String envPredicate = "";
					envPredicate += "        exists p in " + envPreset  + " {\n";
					envPredicate += "          ~$ P p\n";
					envPredicate += "        }\n";
					if (t.getDirection() == Direction.PLUS) {
						if ( !riseDevPredicate.isEmpty() ) {
							riseDevPredicate += "        |\n";
						}
						riseDevPredicate += devPredicate;
						if ( !riseEnvPredicate.isEmpty() ) {
							riseEnvPredicate += "        &\n";
						}
						riseEnvPredicate += envPredicate;
					} else {
						if ( !fallDevPredicate.isEmpty() ) {
							fallDevPredicate += "        |\n";
						}
						fallDevPredicate += devPredicate;
						if ( !fallEnvPredicate.isEmpty() ) {
							fallEnvPredicate += "        &\n";
						}
						fallEnvPredicate += envPredicate;
					}
				}
			}

			if ( !riseDevPredicate.isEmpty() || !fallDevPredicate.isEmpty() ) {
				if (result.isEmpty()) {
					result = "card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n";
				} else {
					result += "|\n";
				}
				result += "(  /* Conformation check for signal \"" + signalFlatName + "\" */\n";
				result += "    (\n";
				result +=          riseDevPredicate;
				result += "    )\n";
				result += "    &\n";
				result += "    (\n";
				result +=          riseEnvPredicate;
				result += "    )\n";
				result += "    |\n";
				result += "    (\n";
				result +=          fallDevPredicate;
				result += "    )\n";
				result += "    &\n";
				result += "    (\n";
				result +=          fallEnvPredicate;
				result += "    )\n";
				result += ")\n";
			}
		}
		return result;
	}

	public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit) {
		this(name, mode, verbosity, solutionMode, solutionNumberLimit, null, null, null);
	}

	public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit, String reach) {
		this(name, mode, verbosity, solutionMode, solutionNumberLimit, reach, null, null);
	}

	public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit,
			String reach, String satisfiableMessage, String unsatisfiableMessage) {
		this.name = name;
		this.mode = mode;
		this.verbosity = verbosity;
		this.solutionMode = solutionMode;
		this.solutionNumberLimit = solutionNumberLimit;
		this.reach = reach;
		this.satisfiableMessage = satisfiableMessage;
		this.unsatisfiableMessage = unsatisfiableMessage;
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

	public SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public int getSolutionNumberLimit() {
		return solutionNumberLimit;
	}

	public String getReach() {
		return reach;
	}

	private String getFlatReach() {
		StringBuffer sb = new StringBuffer(reach.length());
		Matcher matcher = nodeNamePattern.matcher(reach);
		while (matcher.find()) {
			String reference = matcher.group(1);
			String flatName = NamespaceHelper.hierarchicalToFlatName(reference);
			matcher.appendReplacement(sb, "\"" + flatName + "\"");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public String getSatisfiableMessage() {
		return satisfiableMessage;
	}

	public String getUnsatisfiableMessage() {
		return unsatisfiableMessage;
	}

	public String[] getMpsatArguments() {
		ArrayList<String> args = new ArrayList<String>();
		for (String option: getMode().getArgument().split("\\s")) {
			args.add(option);
		}

		if (getMode().hasReach()) {
			try {
				File reach = File.createTempFile("reach", null);
				reach.deleteOnExit();
				FileUtils.dumpString(reach, getFlatReach());
				args.add("-d");
				args.add("@"+reach.getCanonicalPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
