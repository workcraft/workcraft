package org.workcraft.plugins.stg;

public class Wait {

    public enum Type { WAIT1, WAIT0 }

    public final String name;
    public final Signal sig;
    public final Signal ctrl;
    public final Signal san;
    private Type type;

    public Wait(String name, Signal sig, Signal ctrl, Signal san) {
        this.name = name;
        this.sig = sig;
        this.ctrl = ctrl;
        this.san = san;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "WAIT " + name + " (.sig(" + sig.name + "), .ctrl(" + ctrl.name + "), .san(" + san.name + "))";
    }

}
