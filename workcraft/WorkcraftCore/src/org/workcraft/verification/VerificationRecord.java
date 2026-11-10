package org.workcraft.verification;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cached result of verifying a goal. The record is valid as long as the model math layer, the goal exceptions
 * and the hashes of the models that the result depends on are unchanged.
 *
 * @param goalId id of the verified goal
 * @param outcome outcome of the verification: PASS or FAIL, or UNDETERMINED if the verification cannot be
 *                performed on the model (e.g. the model is unsafe)
 * @param tool name of the tool that produced the result
 * @param version version and edition of Workcraft
 * @param os OS description
 * @param message summary of the result
 * @param exceptions text of the exceptions that were in effect for the verification
 * @param modelHash hash of the model math layer
 * @param dependencies the other models that the result depends on (their files and hashes), by the references of the
 *                     circuit components that use them (empty for the environment)
 * @param timestamp time when the verification was completed
 */
public record VerificationRecord(
        String goalId,
        Outcome outcome,
        String timestamp,
        String tool,
        String version,
        String os,
        String message,
        String exceptions,
        String modelHash,
        Map<String, Dependency> dependencies) {

    /**
     * Model that a result depends on.
     *
     * @param file path to the file of the model, which may be absent
     * @param hash hash of the model math layer
     */
    public record Dependency(String file, String hash) {
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum Outcome {
        PASS,
        FAIL,
        UNDETERMINED,
    }

    public VerificationRecord {
        dependencies = (dependencies == null) ? Collections.emptyMap()
                : Collections.unmodifiableMap(new TreeMap<>(dependencies));
    }

    public boolean isPass() {
        return outcome == Outcome.PASS;
    }

    /**
     * The outcome tells whether the property holds, and therefore can be reused instead of verification.
     */
    public boolean isDefinite() {
        return outcome != Outcome.UNDETERMINED;
    }

    /**
     * Time when the verification was completed, or null if it is not available.
     */
    public ZonedDateTime getTime() {
        try {
            return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Description of the tool, Workcraft and OS that produced the result, or empty text if there is none.
     * No tool is used for a result that holds vacuously, so the toolchain is just Workcraft.
     */
    public String getToolchain() {
        boolean hasTool = (tool != null) && !tool.isEmpty();
        boolean hasVersion = (version != null) && !version.isEmpty();
        if (!hasTool && !hasVersion) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        if (hasTool) {
            result.append(tool).append(hasVersion ? " in " : "");
        }
        if (hasVersion) {
            result.append("Workcraft ").append(version);
        }
        if ((os != null) && !os.isEmpty()) {
            result.append(" (").append(os).append(')');
        }
        return result.toString();
    }

    public static String formatTime(ZonedDateTime time) {
        return TIME_FORMATTER.format(time);
    }

}
