package org.workcraft.plugins.mpsat.utils;

import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.types.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class ReachUtils {

    public static VerificationParameters getToolchainPreparationParameters() {
        return new VerificationParameters("Toolchain preparation of data",
                VerificationMode.UNDEFINED, 0,
                null, 0);
    }

    public static VerificationParameters getToolchainCompletionParameters() {
        return new VerificationParameters("Toolchain completion",
                VerificationMode.UNDEFINED, 0,
                null, 0);
    }

    public static VerificationParameters getDeadlockParameters() {
        return new VerificationParameters("Deadlock freeness",
                VerificationMode.DEADLOCK, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount());
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

    public static VerificationParameters getConsistencyParameters() {
        return new VerificationParameters("Consistency",
                VerificationMode.STG_REACHABILITY_CONSISTENCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                REACH_CONSISTENCY, true);
    }

    private static final String REACH_DUMMY_CHECK =
            "exists e in EVENTS {\n" +
            "    is_dummy e\n" +
            "}\n";

    private static final String REPLACEMENT_OUTPUT_PERSISTENCY_EXCEPTIONS =
            "/* insert signal pairs of output persistency exceptions */"; // For example: {"me1_g1", "me1_g2"}, {"me2_g1", "me2_g2"},

    private static final String REACH_OUTPUT_PERSISTENCY =
            "// Checks whether the STG is output-persistent, i.e. no local signal can be disabled by any other signal,\n" +
            "// with the exception of the provided set of pairs of signals (e.g. mutex outputs).\n" +
            REACH_DUMMY_CHECK +
            "? fail \"Output persistency can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    EXCEPTIONS = {" + REPLACEMENT_OUTPUT_PERSISTENCY_EXCEPTIONS + "{\"\"}} \\ {{\"\"}},\n" +
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

    public static VerificationParameters getOutputPersistencyParameters() {
        return getOutputPersistencyParameters(new LinkedList<>());
    }

    public static VerificationParameters getOutputPersistencyParameters(Collection<Pair<String, String>> exceptionPairs) {
        String str = "";
        if (exceptionPairs != null) {
            for (Pair<String, String> exceptionPair: exceptionPairs) {
                str += "{\"" + exceptionPair.getFirst() + "\", \"" + exceptionPair.getSecond() + "\"}, ";
            }
        }
        String reachOutputPersistence = REACH_OUTPUT_PERSISTENCY.replace(REPLACEMENT_OUTPUT_PERSISTENCY_EXCEPTIONS, str);
        return new VerificationParameters("Output persistency",
                VerificationMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachOutputPersistence, true);
    }

    private static final String REACH_DI_INTERFACE =
            "// Checks whether the STG's interface is delay insensitive, i.e. an input transition cannot trigger another input transition\n" +
            REACH_DUMMY_CHECK +
            "? fail \"Delay insensitivity can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    TRINP = tran INPUTS * tran EVENTS\n" +
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

    public static VerificationParameters getDiInterfaceParameters() {
        return new VerificationParameters("Delay insensitive interface",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
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

    public static VerificationParameters getInputPropernessParameters() {
        return new VerificationParameters("Input properness",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                REACH_INPUT_PROPERNESS, true);
    }

    // REACH expression for checking conformation (this is a template, the list of places needs to be updated)
    private static final String REPLACEMENT_CONFORMATION_DEV_PLACES =
            "/* insert device place names here */"; // For example: "p0", "<a-,b+>"

    // Note: New (PNML-based) version of Punf is required to check conformation property. Old version of
    // Punf does not support dead signals, dead transitions and dead places well (e.g. a dead transition
    // may disappear from unfolding), therefore the conformation property cannot be checked reliably.
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
            "    PDEV_NAMES = {" + REPLACEMENT_CONFORMATION_DEV_PLACES + "\"\"} \\ {\"\"},\n" +
            "    // PDEV is the set of places with the names in PDEV_NAMES.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead places correctly.\n" +
            "    PDEV = gather nm in PDEV_NAMES { P nm },\n" +
            "    // PDEV_EXT includes PDEV and places with the names of the form p@num, where p is a place in PDEV.\n" +
            "    // Such places appeared during optimisation of the unfolding prefix due to splitting places\n" +
            "    // incident with multiple read arcs (-r option of punf).\n" +
            "    // Note that such a place must have the same preset and postset (ignoring context) as p.\n" +
            "    PDEV_EXT = PDEV + gather p in PP \".*@[0-9]+\" s.t.\n" +
            "    let name_p=name p, pre_p=pre p, post_p=post p, s_pre_p=pre_p \\ post_p, s_post_p=post_p \\ pre_p {\n" +
            "        exists q in PDEV {\n" +
            "            let name_q=name q, pre_q=pre q, post_q=post q {\n" +
            "                name_p[..len name_q] = name_q + \"@\" &\n" +
            "                pre_q \\ post_q=s_pre_p & post_q \\ pre_q=s_post_p\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
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
    public static VerificationParameters getConformationParameters(Collection<String> devPlaceRefs) {
        String str = devPlaceRefs.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reachConformation = REACH_CONFORMATION.replace(REPLACEMENT_CONFORMATION_DEV_PLACES, str);
        return new VerificationParameters("Conformation",
                VerificationMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachConformation, true);
    }

    // REACH expression for checking if these two pairs of signals can be implemented by a mutex
    private static final String REPLACEMENT_MUTEX_R1 = "/* insert r1 name here */";
    private static final String REPLACEMENT_MUTEX_G1 = "/* insert g1 name here */";
    private static final String REPLACEMENT_MUTEX_R2 = "/* insert r2 name here */";
    private static final String REPLACEMENT_MUTEX_G2 = "/* insert g2 name here */";
    private static final String REACH_MUTEX_IMPLEMENTABILITY_STRICT =
            "// For given signals r1, r2, g1, g2, check whether g1/g2 can be implemented\n" +
            "// by a STRICT mutex with requests r1/r2 and grants g1/g2.\n" +
            "// The properties to check are:\n" +
            "//   r1&~g2 => nxt(g1)\n" +
            "//   ~r1 => ~nxt(g1)\n" +
            "//   r2&g2 => ~nxt(g1)\n" +
            "// (and the symmetric constraints for nxt(g2)).\n" +
            "// Furthemore, the mutual exclusion of the critical sections is checked:\n" +
            "// ~( (r1&g1) & (r2&g2) )\n" +
            "// Note that the latter property does not follow from the above constraints\n" +
            "// for the next state functions of the grants (e.g. in the initial state).\n" +
            "let\n" +
            "    r1s = S\"" + REPLACEMENT_MUTEX_R1 + "\",\n" +
            "    g1s = S\"" + REPLACEMENT_MUTEX_G1 + "\",\n" +
            "    r2s = S\"" + REPLACEMENT_MUTEX_R2 + "\",\n" +
            "    g2s = S\"" + REPLACEMENT_MUTEX_G2 + "\",\n" +
            "    r1 = $r1s,\n" +
            "    g1 = $g1s,\n" +
            "    r2 = $r2s,\n" +
            "    g2 = $g2s,\n" +
            "    g1nxt = 'g1s,\n" +
            "    g2nxt = 'g2s\n" +
            "{\n" +
            "    // constraints on nxt(g1)\n" +
            "    r1 & ~g2 & ~g1nxt  // negation of r1&~g2 => nxt(g1)\n" +
            "    |\n" +
            "    ~r1 & g1nxt        // negation of ~r1 => ~nxt(g1)\n" +
            "    |\n" +
            "    r2 & g2 & g1nxt    // negation of r2&g2 => ~nxt(g1)\n" +
            "    |\n" +
            "    // constraints on nxt(g2)\n" +
            "    r2 & ~g1 & ~g2nxt  // negation of r2&~g1 => nxt(g2)\n" +
            "    |\n" +
            "    ~r2 & g2nxt        // negation of ~r2 => ~nxt(g2)\n" +
            "    |\n" +
            "    r1 & g1 & g2nxt    // negation of r1&g1 => ~nxt(g2)\n" +
            "    |\n" +
            "    // mutual exclusion of critical sections\n" +
            "    r1 & g1 & r2 & g2\n" +
            "}\n";

    private static final String REACH_MUTEX_IMPLEMENTABILITY_RELAXED =
            "// For given signals r1, r2, g1, g2, check whether g1/g2 can be implemented\n" +
            "// by a RELAXED mutex with requests r1/r2 and grants g1/g2.\n" +
            "// The properties to check are:\n" +
            "//   nxt(g1) = r1 & (~r2 | ~g2)\n" +
            "//   nxt(g2) = r2 & (~r1 | ~g1)\n" +
            "// Furthemore, the mutual exclusion of the critical sections is checked:\n" +
            "// ~( (r1&g1) & (r2&g2) )\n" +
            "// Note that the latter property does not follow from the above constraints\n" +
            "// for the next state functions of the grants (e.g. in the initial state).\n" +
            "let\n" +
            "    r1s = S\"" + REPLACEMENT_MUTEX_R1 + "\",\n" +
            "    g1s = S\"" + REPLACEMENT_MUTEX_G1 + "\",\n" +
            "    r2s = S\"" + REPLACEMENT_MUTEX_R2 + "\",\n" +
            "    g2s = S\"" + REPLACEMENT_MUTEX_G2 + "\",\n" +
            "    r1 = $r1s,\n" +
            "    g1 = $g1s,\n" +
            "    r2 = $r2s,\n" +
            "    g2 = $g2s,\n" +
            "    g1nxt = 'g1s,\n" +
            "    g2nxt = 'g2s\n" +
            "{\n" +
            "    (g1nxt ^ (r1 & (~r2 | ~g2)))  // negated definition of nxt(g1)\n" +
            "    |\n" +
            "    (g2nxt ^ (r2 & (~r1 | ~g1)))  // negated definition of nxt(g2)\n" +
            "    |\n" +
            "    r1 & g1 & r2 & g2  // mutual exclusion of critical sections\n" +
            "}\n";

    public static ArrayList<VerificationParameters> getMutexImplementabilityParameters(Collection<Mutex> mutexes) {
        final ArrayList<VerificationParameters> settingsList = new ArrayList<>();
        for (Mutex mutex: mutexes) {
            settingsList.add(getMutexImplementabilityParameters(mutex));
        }
        return settingsList;
    }

    private static VerificationParameters getMutexImplementabilityParameters(Mutex mutex) {
        String reach = getMutexImplementabilityReach()
                .replace(REPLACEMENT_MUTEX_R1, mutex.r1.name)
                .replace(REPLACEMENT_MUTEX_G1, mutex.g1.name)
                .replace(REPLACEMENT_MUTEX_R2, mutex.r2.name)
                .replace(REPLACEMENT_MUTEX_G2, mutex.g2.name);

        return new VerificationParameters("Mutex implementability for place '" + mutex.name + "'",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    private static String getMutexImplementabilityReach() {
        if (StgSettings.getMutexProtocol() == Mutex.Protocol.RELAXED) {
            return REACH_MUTEX_IMPLEMENTABILITY_RELAXED;
        }
        return REACH_MUTEX_IMPLEMENTABILITY_STRICT;
    }

    public static String getBooleanAsString(boolean value) {
        return value ? "true" : "false";
    }

}
