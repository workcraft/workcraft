package org.workcraft.plugins.stg;

public class MutexData {

    public final String name;
    public final String r1;
    public final String g1;
    public final String r2;
    public final String g2;

    public MutexData(String name, String r1, String g1, String r2, String g2) {
        this.name = name;
        this.r1 = r1;
        this.g1 = g1;
        this.r2 = r2;
        this.g2 = g2;
    }

    @Override
    public String toString() {
        return "MUTEX " + name + " (.r1(" + r1 + "), .g1(" + g1 + "), .r2(" + r2 + "), .g2(" + g2 + "))";
    }

}