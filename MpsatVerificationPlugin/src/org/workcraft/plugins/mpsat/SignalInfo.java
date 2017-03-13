package org.workcraft.plugins.mpsat;

public class SignalInfo {

    public final String name;
    public final String setExpr;
    public final String resetExpr;

    public SignalInfo(String name, String setExpr, String resetExpr) {
        this.name = name;
        this.setExpr = setExpr;
        this.resetExpr = resetExpr;
    }

}
