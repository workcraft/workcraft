/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.workcraft.util.FileUtils;

public class MpsatSettings {

    private static final String PROPERTY_FILE_PREFIX = "property";
    private static final String PROPERTY_FILE_EXTENTION = ".re";

    public enum SolutionMode {
        MINIMUM_COST("Minimal cost solution"),
        FIRST("First solution"),
        ALL("First 10 solutions");

        private final String name;

        SolutionMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final String name;
    private final MpsatMode mode;
    private final int verbosity;
    private final SolutionMode solutionMode;
    private final int solutionNumberLimit;
    private final String reach;
    // Relation between the predicate and the property:
    //   true - property holds when predicate is unsatisfiable
    //   false - property holds when predicate is satisfiable
    private final boolean inversePredicate;

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
            "card DUMMY != 0 ? fail \"Output persistency can currently be checked only for STGs without dummies\" :\n" +
            "    exists t1 in tran EVENTS s.t. sig t1 in LOCAL {\n" +
            "        @t1 &\n" +
            "        exists t2 in tran EVENTS s.t. sig t2 != sig t1 & card (pre t1 * (pre t2 \\ post t2)) != 0 {\n" +
            "            @t2 &\n" +
            "            forall t3 in tran EVENTS * (tran sig t1 \\ {t1}) s.t. card (pre t3 * (pre t2 \\ post t2)) = 0 {\n" +
            "                exists p in pre t3 \\ post t2 { ~$p }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n";

    // Reach expression for checking conformation (this is a template, the list of places needs to be updated for each circuit)
    private static final String reachConformationDevPlaces = "// insert device place names here"; // For example: "p0", "<a-,b+>"
/*
            "    PDEV_NAMES={ \"GUID\", \n" +
            "        "  + reachConformationDevPlaces + "\n" +
            "    } \\ {\"GUID\"},\n" +
*/
    private static final String reachConformation =
            // LIMITATIONS (could be checked before parallel composition):
            // - The set of device STG place names is non-empty (this limitation can be easily removed).
            // - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.
            // - The device STG must have no dummies.
            "let\n" +
                // PDEV_NAMES is the set of names of places in the composed STG which originated from the device STG.
                // This set may in fact contain places from the environment STG, e.g. when PCOMP removes duplicate
                // places from the composed STG, it substitutes them with equivalent places that remain.
                // LIMITATION: syntax error if any of these sets is empty.
            "    PDEV_NAMES={\n" +
            "        "  + reachConformationDevPlaces + "\n" +
            "    },\n" +
                  // PDEV is the set of places with the names in PDEV_NAMES.
                  // XML-based PUNF / MPSAT are needed here to process dead places correctly.
            "    PDEV=gather nm in PDEV_NAMES { P nm },\n" +
                  // PDEV_EXT includes PDEV and places with the same preset and postset ignoring context as some place in PDEV
              "    PDEV_EXT=gather p in PLACES s.t.\n" +
              "        p in PDEV\n" +
              "        |\n" +
              "        let pre_p=pre p, post_p=post p, s_pre_p=pre_p \\ post_p, s_post_p=post_p \\ pre_p {\n" +
              "            exists q in PDEV {\n" +
              "                let pre_q=pre q, post_q=post q {\n" +
              "                    pre_q \\ post_q=s_pre_p & post_q \\ pre_q=s_post_p\n" +
              "                }\n" +
              "            }\n" +
              "        }\n" +
              "    { p },\n" +
                  // TDEV is the set of device transitions.
                // XML-based PUNF / MPSAT are needed here to process dead transitions correctly.
                // LIMITATION: each transition in the device must have some arcs, i.e. its preset or postset is non-empty.
              "    TDEV=tran sig (pre PDEV + post PDEV)\n" +
              "{\n" +
                // The device STG must have no dummies.
            "    card (sig TDEV * DUMMY) != 0 ? fail \"Conformance can currently be checked only for device STGs without dummies\" :\n" +
            "    exists t in TDEV s.t. is_output t {\n" +
                      // Check if t is enabled in the device STG.
                      // LIMITATION: The device STG must have no dummies (this limitation is checked above.)
            "        forall p in pre t s.t. p in PDEV_EXT { $p }\n" +
            "        &\n" +
                    // Check if t is enabled in the composed STG (and thus in the environment STG).
            "        ~@ sig t\n" +
            "    }\n" +
            "}\n";

    // Note: New (PNML-based) version of Punf is required to check conformation property. Old version of
    // Punf does not support dead signals, dead transitions and dead places well (e.g. a dead transition
    // may disappear from unfolding), therefore the conformation property cannot be checked reliably.
    public static String genReachConformation(Set<String> devOutputNames, Set<String> devPlaceNames) {
        String devPlaceList = genNameList(devPlaceNames);
        return reachConformation.replaceFirst(reachConformationDevPlaces, devPlaceList);
    }

    private static String genNameList(Collection<String> names) {
        String result = "";
        for (String name: names) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            result += "\"" + name + "\"";
        }
        return result;
    }

    public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit) {
        this(name, mode, verbosity, solutionMode, solutionNumberLimit, null, true);
    }

    public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit,
            String reach, boolean inversePredicate) {
        this.name = name;
        this.mode = mode;
        this.verbosity = verbosity;
        this.solutionMode = solutionMode;
        this.solutionNumberLimit = solutionNumberLimit;
        this.reach = reach;
        this.inversePredicate = inversePredicate;
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

    public boolean getInversePredicate() {
        return inversePredicate;
    }

    public String[] getMpsatArguments(File workingDirectory) {
        ArrayList<String> args = new ArrayList<>();
        for (String option: getMode().getArgument().split("\\s")) {
            args.add(option);
        }

        if (getMode().hasReach()) {
            try {
                File reachFile = null;
                if (workingDirectory == null) {
                    reachFile = FileUtils.createTempFile(PROPERTY_FILE_PREFIX, PROPERTY_FILE_EXTENTION);
                    reachFile.deleteOnExit();
                } else {
                    String prefix = name == null ? PROPERTY_FILE_PREFIX : PROPERTY_FILE_PREFIX + "-" + name.replaceAll("\\s", "_");
                    reachFile = new File(workingDirectory, prefix + PROPERTY_FILE_EXTENTION);
                }
                FileUtils.dumpString(reachFile, getReach());
                args.add("-d");
                args.add("@" + reachFile.getAbsolutePath());
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
            if (solutionNumberLimit > 0) {
                args.add("-a" + Integer.toString(solutionNumberLimit));
            } else {
                args.add("-a");
            }
        }

        return args.toArray(new String[args.size()]);
    }

    public static MpsatSettings getConsistencySettings() {
        return new MpsatSettings("Consistency", MpsatMode.STG_REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.reachConsistency, true);
    }

    public static MpsatSettings getDeadlockSettings() {
        MpsatSettings deadlockSettings = new MpsatSettings("Deadlock freeness", MpsatMode.DEADLOCK, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount());
        return deadlockSettings;
    }

    public static MpsatSettings getPersistencySettings() {
        return new MpsatSettings("Output persistency", MpsatMode.STG_REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.reachSemimodularity, true);
    }

    public static MpsatSettings getCscSettings() {
        return new MpsatSettings("Complete ctate coding", MpsatMode.CSC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

    public static MpsatSettings getNormalcySettings() {
        return new MpsatSettings("Normalcy", MpsatMode.NORMALCY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                null, true);
    }

    public static MpsatSettings getUscSettings() {
        return new MpsatSettings("Unique state coding", MpsatMode.USC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

}
