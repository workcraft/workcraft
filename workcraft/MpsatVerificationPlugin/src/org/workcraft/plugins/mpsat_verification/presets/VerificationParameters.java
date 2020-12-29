package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VerificationParameters {

    private static final String ASSERTION_FILE_PREFIX = "assertion";
    private static final String ASSERTION_FILE_EXTENSION = ".txt";

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

    private final String description;
    private final VerificationMode mode;
    private final int verbosity;
    private final SolutionMode solutionMode;
    private final int solutionNumberLimit;
    private final String expression;
    // Relation between the predicate and the property:
    //   true - property holds when predicate is unsatisfiable
    //   false - property holds when predicate is satisfiable
    private final boolean inversePredicate;

    public VerificationParameters(String description, VerificationMode mode, int verbosity,
            SolutionMode solutionMode, int solutionNumberLimit) {

        this(description, mode, verbosity, solutionMode, solutionNumberLimit, null, true);
    }

    public VerificationParameters(String description, VerificationMode mode, int verbosity,
            SolutionMode solutionMode, int solutionNumberLimit, String expression, boolean inversePredicate) {

        this.description = description;
        this.mode = mode;
        this.verbosity = verbosity;
        this.solutionMode = solutionMode;
        this.solutionNumberLimit = solutionNumberLimit;
        this.expression = expression;
        this.inversePredicate = inversePredicate;
    }

    public String getDescription() {
        return description;
    }

    public String getPropertyCheckMessage(boolean propertyHolds) {
        String propertyName = getDescription();
        if ((propertyName == null) || propertyName.isEmpty()) {
            propertyName = "Property";
        }

        return propertyName + (propertyHolds ? " holds." :  " is violated.");
    }

    public VerificationMode getMode() {
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

    public boolean isInversePredicate() {
        return inversePredicate;
    }

    public String[] getMpsatArguments(File workingDirectory) {
        ArrayList<String> args = new ArrayList<>(getMode().getArguments());

        String expression = getExpression();
        if (expression != null) {
            try {
                File assertionFile = getDescriptiveFile(workingDirectory, ASSERTION_FILE_PREFIX, ASSERTION_FILE_EXTENSION);
                FileUtils.dumpString(assertionFile, expression);
                if (MpsatVerificationSettings.getDebugReach()) {
                    LogUtils.logInfo("REACH expression to check");
                    LogUtils.logMessage(expression);
                }

                args.add("-d");
                args.add("@" + assertionFile.getAbsolutePath());
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
                args.add("-a" + solutionNumberLimit);
            } else {
                args.add("-a");
            }
            break;
        }

        return args.toArray(new String[0]);
    }

    public File getDescriptiveFile(File directory, String prefix, String extension) {
        return new File(directory, prefix + getDescriptiveSuffix() + extension);
    }

    public String getDescriptiveSuffix() {
        return description == null ? "" : "-" + description.replaceAll("\\s", "_");
    }

}
