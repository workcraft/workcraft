package org.workcraft.plugins.petri_expression.presets;

public class ExpressionParameters {

    public enum Mode {
        FAST("Fast heuristic"),
        EXACT("Exact solution");

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
