package org.workcraft.plugins.stg;

public class MutexData {
    public String ref;
    public String r1;
    public String g1;
    public String r2;
    public String g2;

    @Override
    public String toString() {
        return ref + " (.r1(" + r1 + "), .g1(" + g1 + "), .r2(" + r2 + "), .g2(" + g2 + "))";
    }

}