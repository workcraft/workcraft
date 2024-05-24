package org.workcraft.plugins.stg;

public class Wait {

    public enum Type { WAIT1, WAIT0 }

    public final String name;
    public final Signal sig;
    public final Signal ctrl;
    public final Signal san;
    public final Type type;

    public Wait(String name, Type type, Signal sig, Signal ctrl, Signal san) {
        this.name = name;
        this.type = type;
        this.sig = sig;
        this.ctrl = ctrl;
        this.san = san;
    }

    @Override
    public String toString() {
        return type.toString() + ' ' + name + " (.sig(" + sig.name + "), .ctrl(" + ctrl.name + "), .san(" + san.name + "))";
    }

    public String getSanSetFormula() {
        return ctrl.name + " * " + sig.name + (type == Wait.Type.WAIT0 ? "'" : "");
    }

    public String getSanResetFormula() {
        return ctrl.name + "'";
    }

}
