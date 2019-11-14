package org.workcraft.plugins.mpsat;

import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VerificationParameters {

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
    private final VerificationMode mode;
    private final int verbosity;
    private final SolutionMode solutionMode;
    private final int solutionNumberLimit;
    private final String expression;
    // Relation between the predicate and the property:
    //   true - property holds when predicate is unsatisfiable
    //   false - property holds when predicate is satisfiable
    private final boolean inversePredicate;

    public VerificationParameters(String name, VerificationMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit) {
        this(name, mode, verbosity, solutionMode, solutionNumberLimit, null, true);
    }

    public VerificationParameters(String name, VerificationMode mode, int verbosity, SolutionMode solutionMode, int solutionNumberLimit,
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
                    String prefix = name == null ? PROPERTY_FILE_PREFIX
                            : PROPERTY_FILE_PREFIX + "-" + name.replaceAll("\\s", "_");

                    reachFile = new File(workingDirectory, prefix + PROPERTY_FILE_EXTENTION);
                }
                String reachExpression = getExpression();
                FileUtils.dumpString(reachFile, reachExpression);
                if (MpsatVerificationSettings.getDebugReach()) {
                    LogUtils.logInfo("Reach expression to check");
                    LogUtils.logMessage(reachExpression);
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
                args.add("-a" + solutionNumberLimit);
            } else {
                args.add("-a");
            }
            break;
        }

        return args.toArray(new String[args.size()]);
    }

}
