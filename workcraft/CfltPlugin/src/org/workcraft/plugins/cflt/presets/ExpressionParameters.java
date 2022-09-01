package org.workcraft.plugins.cflt.presets;

public class ExpressionParameters {

    public enum Mode {
        FAST_MAX("Maximum heuristic"),
        FAST_MIN("Minimum heuristic"),
        FAST_SEQ("Sequence heuristic"),
        SLOW_EXACT("Expensive exact ECC");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final String description;
    private final Mode mode;
    private final String expression;

    public ExpressionParameters(String description, Mode mode, String expression) {
        this.description = description;
        this.mode = mode;
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public Mode getMode() {
        return mode;
    }

    public String getExpression() {
        return expression;
    }

}
