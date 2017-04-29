package org.workcraft.plugins.mpsat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.plugins.stg.MutexData;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Pair;

public class MpsatParameters {

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

    public static class SignalInfo {
        public final String name;
        public final String setExpr;
        public final String resetExpr;

        public SignalInfo(String name, String setExpr, String resetExpr) {
            this.name = name;
            this.setExpr = setExpr;
            this.resetExpr = resetExpr;
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

    public MpsatParameters(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit) {
        this(name, mode, verbosity, solutionMode, solutionNumberLimit, null, true);
    }

    public MpsatParameters(String name, MpsatMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit,
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
                if (MpsatSettings.getDebugReach()) {
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

    public static MpsatParameters getToolchainPreparationSettings() {
        return new MpsatParameters("Toolchain preparation of data",
                MpsatMode.UNDEFINED, 0, null, 0);
    }

    public static MpsatParameters getToolchainCompletionSettings() {
        return new MpsatParameters("Toolchain completion",
                MpsatMode.UNDEFINED, 0, null, 0);
    }

    public static MpsatParameters getDeadlockSettings() {
        return new MpsatParameters("Deadlock freeness", MpsatMode.DEADLOCK, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount());
    }

    private static final String REACH_DEADLOCK =
            "forall t in TRANSITIONS { ~@t }\n";

    public static MpsatParameters getDeadlockReachSettings() {
        return new MpsatParameters("Deadlock freeness", MpsatMode.REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                REACH_DEADLOCK, true);
    }

    private static final String REACH_DEADLOCK_WITHOUT_MAXIMAL_DUMMY =
            "// Ensure that the explored configurations have no maximal dummies, i.e. whenever a dummy\n" +
            "// event is included into a configuration, some of its causal successors is also included.\n" +
            "forall e in ev DUMMY \\ CUTOFFS {\n" +
            "    $e -> exists f in post post e \\ CUTOFFS { $f }\n" +
            "}\n" +
            "&\n" +
            "// e is 'stubborn' if |(*e)*\\CUTOFFS|<=1;\n" +
            "// to reach a deadlock, a non-cut-off stubborn e must fire whenever **e fired;\n" +
            "// for a cut-off subborn e, **e cannot be a part of a deadlocked configuration.\n" +
            "forall e in EVENTS {\n" +
            "    let Pe = pre e, Pe_P = post Pe \\ CUTOFFS, stb = card Pe_P <= 1 {\n" +
            "        exists f in pre Pe { ~$f }\n" +
            "        |\n" +
            "        (stb ? ~is_cutoff e & $e : exists f in Pe_P { $f })\n" +
            "    }\n" +
            "}\n";

    public static MpsatParameters getDeadlockWithoutMaximalDummyReachSettings() {
        return new MpsatParameters("Deadlock freeness without maximal dummies", MpsatMode.REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                REACH_DEADLOCK_WITHOUT_MAXIMAL_DUMMY, true);
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

    public static MpsatParameters getConsistencySettings() {
        return new MpsatParameters("Consistency", MpsatMode.STG_REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                REACH_CONSISTENCY, true);
    }

    private static final String REACH_DUMMY_CHECK =
            "exists e in EVENTS {\n" +
            "    is_dummy e\n" +
            "}\n";

    // Reach expression for checking if these two pairs of signals can be implemented by a mutex
    private static final String REACH_MUTEX_R1 = "/* insert r1 name here */";
    private static final String REACH_MUTEX_G1 = "/* insert g1 name here */";
    private static final String REACH_MUTEX_R2 = "/* insert r2 name here */";
    private static final String REACH_MUTEX_G2 = "/* insert g2 name here */";
    private static final String REACH_IMPLICIT_MUTEX =
            "// For given signals r1, r2, g1, g2, check whether g1/g2 can be implemented by a mutex\n" +
            "// with requests r1/r2 and grants g1/g2. It is very similar to the \"strict implementation\",\n" +
            "// i.e. we are looking for a \"bad\" state where the next-state values of the grants differ\n" +
            "// from [g1] = r1 & ~g2 and [g2] = r2 & ~g1.\n" +
            "let\n" +
            "    r1 = S\"" + REACH_MUTEX_R1 + "\",\n" +
            "    g1 = S\"" + REACH_MUTEX_G1 + "\",\n" +
            "    r2 = S\"" + REACH_MUTEX_R2 + "\",\n" +
            "    g2 = S\"" + REACH_MUTEX_G2 + "\"\n" +
            "{\n" +
            "    ('g1 ^ ($r1 & ~$g2))\n" +
            "    |\n" +
            "    ('g2 ^ ($r2 & ~$g1))\n" +
            "}\n";

    public static MpsatParameters getImplicitMutexSettings(MutexData mutexData) {
        String reach = REACH_IMPLICIT_MUTEX
                .replace(REACH_MUTEX_R1, mutexData.r1)
                .replace(REACH_MUTEX_G1, mutexData.g1)
                .replace(REACH_MUTEX_R2, mutexData.r2)
                .replace(REACH_MUTEX_G2, mutexData.g2);
        return new MpsatParameters("Non-persistency implementable by mutex " + mutexData, MpsatMode.STG_REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                reach, true);
    }

    private static final String REACH_OUTPUT_PERSISTENCY_EXCEPTIONS =
            "/* insert signal pairs of output persistency exceptions */"; // For example: {"me1_g1", "me1_g2"}, {"me2_g1", "me2_g2"},

    private static final String REACH_OUTPUT_PERSISTENCY =
            "// Checks whether the STG is output persistent, i.e. no local signal can be disabled by any other signal,\n" +
            "// with the exception of the provided set of pairs of signals (e.g. mutex outputs).\n" +
            REACH_DUMMY_CHECK +
            "? fail \"Output persistency can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    EXCEPTIONS = {" + REACH_OUTPUT_PERSISTENCY_EXCEPTIONS + "{\"\"}} \\ {{\"\"}},\n" +
            "    SIGE = gather pair in EXCEPTIONS {\n" +
            "        gather str in pair { S str }\n" +
            "    },\n" +
            "    TR = tran EVENTS,\n" +
            "    TRL = tran LOCAL * TR,\n" +
            "    TRPT = gather t in TRL s.t. ~is_minus t { t },\n" +
            "    TRMT = gather t in TRL s.t. ~is_plus t { t }\n" +
            "{\n" +
            "    exists t_loc in TRL {\n" +
            "        let\n" +
            "            pre_t_loc = pre t_loc,\n" +
            "            OTHER_LOC = (tran sig t_loc \\ {t_loc}) * (is_plus t_loc ? TRPT : is_minus t_loc ? TRMT : TR) {\n" +
            "            // Check if some t can disable t_loc without enabling any other transition labelled by sig t_loc.\n" +
            "            exists t in post pre_t_loc * TR s.t. sig t != sig t_loc &\n" +
            "                    ~({sig t, sig t_loc} in SIGE) & card ((pre t \\ post t) * pre_t_loc) != 0 {\n" +
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

    public static MpsatParameters getOutputPersistencySettings() {
        return getOutputPersistencySettings(new LinkedList<Pair<String, String>>());
    }

    public static MpsatParameters getOutputPersistencySettings(Collection<Pair<String, String>> exceptions) {
        String str = "";
        if (exceptions != null) {
            for (Pair<String, String> exception: exceptions) {
                str += "{\"" + exception.getFirst() + "\", \"" + exception.getSecond() + "\"}, ";
            }
        }
        String reachOutputPersistence = REACH_OUTPUT_PERSISTENCY.replace(REACH_OUTPUT_PERSISTENCY_EXCEPTIONS, str);
        return new MpsatParameters("Output persistency", MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                reachOutputPersistence, true);
    }

    private static final String REACH_DI_INTERFACE =
            "// Checks whether the STG's interface is delay insensitive, i.e. an input transition cannot trigger another input transition\n" +
            REACH_DUMMY_CHECK +
            "? fail \"Delay insensitivity can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    TRINP = tran INPUTS * tran EVENTS {" +
            "{\n" +
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

    public static MpsatParameters getDiInterfaceSettings() {
        return new MpsatParameters("Delay insensitive interface", MpsatMode.STG_REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                REACH_DI_INTERFACE, true);
    }

    private static final String REACH_INPUT_PROPERNESS =
            "// Checks whether the STG is input proper, i.e. no input can be triggered by an internal signal or disabled by a local signal.\n" +
            REACH_DUMMY_CHECK +
            "? fail \"Input properness can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    TR = tran EVENTS,\n" +
            "    TRINP = tran INPUTS * TR,\n" +
            "    TRI = tran INTERNAL * TR,\n" +
            "    TRL = tran LOCAL * TR,\n" +
            "    TRPT = gather t in TRINP s.t. ~is_minus t { t },\n" +
            "    TRMT = gather t in TRINP s.t. ~is_plus t { t }\n" +
            "{\n" +
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

    public static MpsatParameters getInputPropernessSettings() {
        return new MpsatParameters("Input properness", MpsatMode.STG_REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                REACH_INPUT_PROPERNESS, true);
    }

    // Reach expression for checking conformation (this is a template, the list of places needs to be updated for each circuit)
    private static final String REACH_CONFORMATION_DEV_PLACES =
            "/* insert device place names here */"; // For example: "p0", "<a-,b+>"

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
            "    PDEV_NAMES = {" + REACH_CONFORMATION_DEV_PLACES + "\"\"} \\ {\"\"},\n" +
            "    // PDEV is the set of places with the names in PDEV_NAMES.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead places correctly.\n" +
            "    PDEV = gather nm in PDEV_NAMES { P nm },\n" +
            "    // PDEV_EXT includes PDEV and places with the same preset and postset ignoring context as some place in PDEV\n" +
            "    PDEV_EXT = gather p in PLACES s.t.\n" +
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
            "    TDEV = tran sig (pre PDEV + post PDEV)\n" +
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

    // Note: New (PNML-based) version of Punf is required to check conformation property. Old version of
    // Punf does not support dead signals, dead transitions and dead places well (e.g. a dead transition
    // may disappear from unfolding), therefore the conformation property cannot be checked reliably.
    public static MpsatParameters getConformationSettings(Collection<String> devPlaceNames) {
        String str = "";
        for (String name: devPlaceNames) {
            str += "\"" + name + "\", ";
        }
        String reachConformation = REACH_CONFORMATION.replace(REACH_CONFORMATION_DEV_PLACES, str);
        return new MpsatParameters("Interface conformation", MpsatMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                reachConformation, true);
    }

    // Reach expression for checking strict implementation
    private static final String REACH_STRICT_IMPLEMENTATION_SIGNAL =
            "/* insert signal name here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR =
            "/* insert complex gate expression here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR_SET =
            "/* insert generalised C-element set function here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR_RESET =
            "/* insert generalised C-element reset function here */";

    private static final String REACH_STRICT_IMPLEMENTATION_COMPLEX_GATE =
            "('S\"" + REACH_STRICT_IMPLEMENTATION_SIGNAL + "\" ^ (" + REACH_STRICT_IMPLEMENTATION_EXPR + "))";

    private static final String REACH_STRICT_IMPLEMENTATION_GENERALISED_CELEMENT =
            "let\n" +
            "    signal=S\"" + REACH_STRICT_IMPLEMENTATION_SIGNAL + "\",\n" +
            "    setExpr=" + REACH_STRICT_IMPLEMENTATION_EXPR_SET + ",\n" +
            "    resetExpr=" + REACH_STRICT_IMPLEMENTATION_EXPR_RESET + " {\n" +
            "    (@signal & ~setExpr & ~$signal) | (setExpr & ~'signal) |\n" +
            "    (@signal & ~resetExpr & $signal) | (resetExpr & 'signal)\n" +
            "}\n";

    private static final String REACH_STRICT_IMPLEMENTATION =
            "// Checks the STG is strictly implemented by a circuit.\n";

    public static MpsatParameters getStrictImplementationReachSettings(Collection<SignalInfo> signalInfos) {
        String reachStrictImplementation = REACH_STRICT_IMPLEMENTATION;
        boolean isFirstSignal = true;
        for (SignalInfo signalInfo: signalInfos) {
            boolean isComplexGate = (signalInfo.resetExpr == null) || signalInfo.resetExpr.isEmpty();
            String s = isComplexGate ? REACH_STRICT_IMPLEMENTATION_COMPLEX_GATE : REACH_STRICT_IMPLEMENTATION_GENERALISED_CELEMENT;
            s = s.replace(REACH_STRICT_IMPLEMENTATION_SIGNAL, signalInfo.name);
            if (isComplexGate) {
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR, signalInfo.setExpr);
            } else {
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR_SET, signalInfo.setExpr);
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR_RESET, signalInfo.resetExpr);
            }
            if (!isFirstSignal) {
                reachStrictImplementation += "\n|\n";
            }
            reachStrictImplementation += s;
            isFirstSignal = false;
        }
        return new MpsatParameters("Strict implementation", MpsatMode.STG_REACHABILITY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                reachStrictImplementation, true);
    }

    public static MpsatParameters getCscSettings() {
        return new MpsatParameters("Complete state coding", MpsatMode.CSC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

    public static MpsatParameters getUscSettings() {
        return new MpsatParameters("Unique state coding", MpsatMode.USC_CONFLICT_DETECTION, 0,
                SolutionMode.ALL, -1 /* unlimited */, null, true);
    }

    public static MpsatParameters getNormalcySettings() {
        return new MpsatParameters("Normalcy", MpsatMode.NORMALCY, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount(),
                null, true);
    }

    public static MpsatParameters getEmptyAssertionSettings() {
        return new MpsatParameters("Empty assertion", MpsatMode.ASSERTION, 0,
                SolutionMode.MINIMUM_COST, 0, "", true);
    }

}
