package org.workcraft.verification;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cached result of verifying a goal. The record is valid as long as the validity hash (that covers the model
 * math layer and the goal exceptions) and the hashes of the models that the result depends on are unchanged.
 *
 * @param goalId id of the verified goal
 * @param outcome definite outcome of the verification
 * @param tool name of the tool that produced the result
 * @param message summary of the result
 * @param exceptions text of the exceptions that were in effect for the verification
 * @param modelHash hash of the model math layer
 * @param exceptionsHash hash of the exceptions text
 * @param validityHash hash that combines the model hash and the exceptions hash
 * @param dependencies the other models that the result depends on (their files and hashes), by their names
 * @param timestamp time when the verification was completed
 */
public record VerificationRecord(String goalId, Outcome outcome, String tool, String message, String exceptions,
        String modelHash, String exceptionsHash, String validityHash, Map<String, Dependency> dependencies,
        String timestamp) {

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
    }

    public VerificationRecord {
        dependencies = (dependencies == null) ? Collections.emptyMap()
                : Collections.unmodifiableMap(new TreeMap<>(dependencies));
    }

    public boolean isPass() {
        return outcome == Outcome.PASS;
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

    public static String formatTime(ZonedDateTime time) {
        return TIME_FORMATTER.format(time);
    }

    /**
     * Time when the verification was completed in a human-readable form.
     */
    public String getFormattedTime() {
        ZonedDateTime time = getTime();
        return time == null ? "" : formatTime(time);
    }

}
