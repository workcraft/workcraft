package org.workcraft.formula.encoding.onehot;

public class AndFunction<BooleanNumber> {

    private final BooleanNumber var1Number;
    private final BooleanNumber var2Number;

    public AndFunction(BooleanNumber var1Number,
            BooleanNumber var2Number) {
        this.var1Number = var1Number;
        this.var2Number = var2Number;
    }

    public BooleanNumber getVar1Number() {
        return var1Number;
    }

    public BooleanNumber getVar2Number() {
        return var2Number;
    }
}
