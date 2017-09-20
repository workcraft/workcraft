package org.workcraft.formula;

public class Literal implements BooleanFormula {

    private BooleanVariable variable;
    private boolean negation;

    public Literal() {
    }

    public static final Literal ZERO = new Literal(new FreeVariable("0"));
    public static final Literal ONE = new Literal(new FreeVariable("1"));

    public Literal(BooleanVariable variable) {
        this.variable = variable;
    }

    public Literal(BooleanVariable variable, boolean negation) {
        this.variable = variable;
        this.negation = negation;
    }

    public Literal(String varName) {
        this(new FreeVariable(varName));
    }

    public void setVariable(BooleanVariable variable) {
        this.variable = variable;
    }

    public BooleanVariable getVariable() {
        return variable;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    public boolean getNegation() {
        return negation;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return (negation ? BooleanOperations.not(variable) : variable).accept(visitor);
    }

}
