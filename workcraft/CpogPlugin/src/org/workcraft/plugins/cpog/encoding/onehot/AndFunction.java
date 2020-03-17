package org.workcraft.plugins.cpog.encoding.onehot;

public class AndFunction<T> {

    private final T var1Number;
    private final T var2Number;

    public AndFunction(T var1Number,
            T var2Number) {
        this.var1Number = var1Number;
        this.var2Number = var2Number;
    }

    public T getVar1Number() {
        return var1Number;
    }

    public T getVar2Number() {
        return var2Number;
    }
}
