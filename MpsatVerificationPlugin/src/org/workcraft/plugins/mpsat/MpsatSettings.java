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
import org.workcraft.util.LogUtils;

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
    private final String expression;
    // Relation between the predicate and the property:
    //   true - property holds when predicate is unsatisfiable
    //   false - property holds when predicate is satisfiable
    private final boolean inversePredicate;

    public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit) {
        this(name, mode, verbosity, solutionMode, solutionNumberLimit, null, true);
    }

    public MpsatSettings(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit,
            String expression, boolean inversePredicate) {
        this.name = name;
        this.mode = mode;
        this.verbosity = verbosity;
        this.solutionMode = solutionMode;
        this.solutionNumberLimit = solutionNumberLimit;
        this.expression = expression;
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

    public String getExpression() {
        return expression;
    }

    public boolean getInversePredicate() {
        return inversePredicate;
    }

    public String[] getMpsatArguments(File workingDirectory) {
        ArrayList<String> args = new ArrayList<>();
        for (String option: getMode().getArgument().split("\\s")) {
            args.add(option);
        }

        if (getMode().hasExpression()) {
            try {
                File reachFile = null;
                if (workingDirectory == null) {
                    reachFile = FileUtils.createTempFile(PROPERTY_FILE_PREFIX, PROPERTY_FILE_EXTENTION);
                    reachFile.deleteOnExit();
                } else {
                    String prefix = name == null ? PROPERTY_FILE_PREFIX : PROPERTY_FILE_PREFIX + "-" + name.replaceAll("\\s", "_");
                    reachFile = new File(workingDirectory, prefix + PROPERTY_FILE_EXTENTION);
                }
                String reachExpression = getExpression();
                FileUtils.dumpString(reachFile, reachExpression);
                if (MpsatUtilitySettings.getDebugReach()) {
                    LogUtils.logInfoLine("Reach expression to check");
                    LogUtils.logMessageLine(reachExpression);
                }

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

    public static MpsatSettings getDeadlockSettings() {
        return new MpsatSettings("Deadlock freeness", MpsatMode.DEADLOCK, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount());
    }

    private static final String REACH_DEADLOCK =
            "forall t in TRANSITIONS { ~@t }\n";

    public static MpsatSettings getDeadlockReachSettings() {
        return new MpsatSettings("Deadlock freeness", MpsatMode.REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_DEADLOCK, true);
    }

    private static final String REACH_CONSISTENCY =
            "// Checks whether the STG is consistent, i.e. rising and falling transitions of every signal alternate in all traces\n" +
            "exists s in SIGNALS \\ DUMMY {\n" +
            "    let Es = ev s {\n" +
            "        $s & exists e in Es s.t. is_plus e { @e }\n" +
            "        |\n" +
            "        ~$s & exists e in Es s.t. is_minus e { @e }\n" +
            "    }\n" +
            "}\n";

    public static MpsatSettings getConsistencySettings() {
        return new MpsatSettings("Consistency", MpsatMode.STG_REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_CONSISTENCY, true);
    }

    private static final String REACH_OUTPUT_PERSISTENCY =
            "// Checks whether the STG is output persistent, i.e. no local signal can be disabled by any other signal.\n" +
            "card DUMMY != 0 ? fail \"Output persistency can be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    TR = tran EVENTS,\n" +
            "    TRL = tran LOCAL * TR,\n" +
            "    TRPT = gather t in TRL s.t. ~is_minus t { t },\n" +
            "    TRMT = gather t in TRL s.t. ~is_plus t { t } {\n" +
            "    exists t_loc in TRL {\n" +
            "        let\n" +
            "            pre_t_loc = pre t_loc,\n" +
            "            OTHER_LOC = (tran sig t_loc \\ {t_loc}) * (is_plus t_loc ? TRPT : is_minus t_loc ? TRMT : TR) {\n" +
            "            // Check if some t can disable t_loc without enabling any other transition labelled by sig t_loc.\n" +
            "            exists t in post pre_t_loc * TR s.t. sig t != sig t_loc & card ((pre t \\ post t) * pre_t_loc) != 0 {\n" +
            "                forall t_loc1 in OTHER_LOC s.t. card (pre t_loc1 * (pre t \\ post t)) = 0 {\n" +
            "                    exists p in pre t_loc1 \\ post t { ~$p }\n" +
            "                }\n" +
            "                &\n" +
            "                @t\n" +
            "            }\n" +
            "            &\n" +
            "            @t_loc\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    public static MpsatSettings getOutputPersistencySettings() {
        return new MpsatSettings("Output persistency", MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_OUTPUT_PERSISTENCY, true);
    }

    public static MpsatSettings getHazardSettings() {
        return new MpsatSettings("Hazard freeness", MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_OUTPUT_PERSISTENCY, true);
    }

    private static final String REACH_DI_INTERFACE =
            "// Checks whether the STG's interface is delay insensitive, i.e. an input transition cannot trigger another input transition\n" +
            "card DUMMY != 0 ? fail \"Delay insensitivity can currently be checked only for STGs without dummies\" :\n" +
            "let TRINP = tran INPUTS * tran EVENTS {\n" +
            "    exists ti in TRINP {\n" +
            "        let pre_ti = pre ti {\n" +
            "            // Check if some ti_trig can trigger ti\n" +
            "            exists ti_trig in pre pre_ti * TRINP s.t. sig ti_trig != sig ti & card((post ti_trig \\ pre ti_trig) * pre_ti) != 0 {\n" +
            "                forall p in pre_ti \\ post ti_trig { $p }\n" +
            "                &\n" +
            "                @ti_trig\n" +
            "            }\n" +
            "            &\n" +
            "            ~@sig ti\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    public static MpsatSettings getDiInterfaceSettings() {
        return new MpsatSettings("Delay insensitive interface", MpsatMode.STG_REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_DI_INTERFACE, true);
    }

    private static final String REACH_INPUT_PROPERNESS =
            "// Checks whether the STG is input proper, i.e. no input can be triggered by an internal signal or disabled by a local signal.\n" +
            "card DUMMY != 0 ? fail \"Input properness can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    TR = tran EVENTS,\n" +
            "    TRINP = tran INPUTS * TR,\n" +
            "    TRI = tran INTERNAL * TR,\n" +
            "    TRL = tran LOCAL * TR,\n" +
            "    TRPT = gather t in TRINP s.t. ~is_minus t { t },\n" +
            "    TRMT = gather t in TRINP s.t. ~is_plus t { t } {\n" +
            "    exists t_inp in TRINP {\n" +
            "        let\n" +
            "            pre_t_inp = pre t_inp,\n" +
            "            OTHER_INP = (tran sig t_inp \\ {t_inp}) * (is_plus t_inp ? TRPT : is_minus t_inp ? TRMT : TR) {\n" +
            "            // Check if some t_int can trigger t_inp.\n" +
            "            exists t_int in pre pre_t_inp * TRI s.t. card((post t_int \\ pre t_int) * pre_t_inp) != 0 {\n" +
            "                forall p in pre_t_inp \\ post t_int { $p }\n" +
            "                &\n" +
            "                @t_int\n" +
            "            }\n" +
            "            &\n" +
            "            ~@sig t_inp\n" +
            "            |\n" +
            "            // Check if some t_loc can disable t_inp without enabling any other transition labelled by sig t_inp.\n" +
            "            exists t_loc in post pre_t_inp * TRL s.t. card((pre t_loc \\ post t_loc) * pre_t_inp) !=0 {\n" +
            "                forall t_inp1 in OTHER_INP s.t. card (pre t_inp1 * (pre t_loc \\ post t_loc)) = 0 {\n" +
            "                    exists p in pre t_inp1 \\ post t_loc { ~$p }\n" +
            "                }\n" +
            "                &\n" +
            "                @t_loc\n" +
            "            }\n" +
            "            &\n" +
            "            @t_inp\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    public static MpsatSettings getInputPropernessSettings() {
        return new MpsatSettings("Input properness", MpsatMode.STG_REACHABILITY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                MpsatSettings.REACH_INPUT_PROPERNESS, true);
    }

    // Reach expression for checking conformation (this is a template, the list of places needs to be updated for each circuit)
    private static final String REACH_CONFORMATION_DEV_PLACES = "// insert device place names here"; // For example: "p0", "<a-,b+>"

    private static final String REACH_CONFORMATION =
            "// Check a device STG for conformation to its environment STG.\n" +
            "// LIMITATIONS (could be checked before parallel composition):\n" +
            "// - The set of device STG place names is non-empty (this limitation can be easily removed).\n" +
            "// - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.\n" +
            "// - The device STG must have no dummies.\n" +
            "let\n" +
            "     // PDEV_NAMES is the set of names of places in the composed STG which originated from the device STG.\n" +
            "     // This set may in fact contain places from the environment STG, e.g. when PCOMP removes duplicate\n" +
            "     // places from the composed STG, it substitutes them with equivalent places that remain.\n" +
            "     // LIMITATION: syntax error if any of these sets is empty.\n" +
            "    PDEV_NAMES={\n" +
            "        " + REACH_CONFORMATION_DEV_PLACES + "\n" +
            "    },\n" +
            "    // PDEV is the set of places with the names in PDEV_NAMES.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead places correctly.\n" +
            "    PDEV=gather nm in PDEV_NAMES { P nm },\n" +
            "    // PDEV_EXT includes PDEV and places with the same preset and postset ignoring context as some place in PDEV\n" +
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
            "    // TDEV is the set of device transitions.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead transitions correctly.\n" +
            "    // LIMITATION: each transition in the device must have some arcs, i.e. its preset or postset is non-empty.\n" +
            "    TDEV=tran sig (pre PDEV + post PDEV)\n" +
            "{\n" +
            "     // The device STG must have no dummies.\n" +
            "    card (sig TDEV * DUMMY) != 0 ? fail \"Conformation can currently be checked only for device STGs without dummies\" :\n" +
            "    exists t in TDEV s.t. is_output t {\n" +
            "         // Check if t is enabled in the device STG.\n" +
            "         // LIMITATION: The device STG must have no dummies (this limitation is checked above.)\n" +
            "        forall p in pre t s.t. p in PDEV_EXT { $p }\n" +
            "        &\n" +
            "         // Check if t is enabled in the composed STG (and thus in the environment STG).\n" +
            "        ~@ sig t\n" +
            "    }\n" +
            "}\n";

    public static MpsatSettings getConformationSettings(Set<String> devOutputNames, Set<String> devPlaceNames) {
        String reachConformation = genReachConformation(devOutputNames, devPlaceNames);
        if (MpsatUtilitySettings.getDebugReach()) {
            System.out.println("\nReach expression for the interface conformation property:");
            System.out.println(reachConformation);
        }
        return new MpsatSettings("Interface conformation", MpsatMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                reachConformation, true);
    }

    // Note: New (PNML-based) version of Punf is required to check conformation property. Old version of
    // Punf does not support dead signals, dead transitions and dead places well (e.g. a dead transition
    // may disappear from unfolding), therefore the conformation property cannot be checked reliably.
    private static String genReachConformation(Set<String> devOutputNames, Set<String> devPlaceNames) {
        String devPlaceList = genNameList(devPlaceNames);
        return REACH_CONFORMATION.replaceFirst(REACH_CONFORMATION_DEV_PLACES, devPlaceList);
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

    public static MpsatSettings getCscSettings() {
        return new MpsatSettings("Complete state coding", MpsatMode.CSC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

    public static MpsatSettings getUscSettings() {
        return new MpsatSettings("Unique state coding", MpsatMode.USC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

    public static MpsatSettings getNormalcySettings() {
        return new MpsatSettings("Normalcy", MpsatMode.NORMALCY, 0,
                MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
                null, true);
    }

    public static MpsatSettings getEmptyAssertionSettings() {
        return new MpsatSettings("Empty assertion", MpsatMode.ASSERTION, 0,
                SolutionMode.MINIMUM_COST, 0, "", true);
    }

}
