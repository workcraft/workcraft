package org.workcraft.plugins.stg;

public class Mutex {

    public enum Protocol {
        EARLY("Early"),
        LATE("Late");

        private final String name;

        Protocol(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public final String name;
    public final Signal r1;
    public final Signal g1;
    public final Signal r2;
    public final Signal g2;
    public final Protocol protocol;

    public Mutex(String name, Signal r1, Signal g1, Signal r2, Signal g2, Protocol protocol) {
        this.name = name;
        this.r1 = r1;
        this.g1 = g1;
        this.r2 = r2;
        this.g2 = g2;
        this.protocol = protocol;
    }

    public String getG1SetFormula() {
        return protocol == Mutex.Protocol.EARLY
                ? r1.name + " * !(" + g2.name + " * " + r2.name + ")"
                : r1.name + " * !" + g2.name;
    }

    public String getG1ResetFormula() {
        return '!' + r1.name;
    }

    public String getG2SetFormula() {
        return protocol == Mutex.Protocol.EARLY
                ? r2.name + " * !(" + g1.name + " * " + r1.name + ")"
                : r2.name + " * !" + g1.name;
    }

    public String getG2ResetFormula() {
        return '!' + r2.name;
    }

    @Override
    public String toString() {
        String r1Name = (r1 == null) || (r1.name == null) ? "" : r1.name;
        String g1Name = (g1 == null) || (g1.name == null) ? "" : g1.name;
        String r2Name = (r2 == null) || (r2.name == null) ? "" : r2.name;
        String g2Name = (g2 == null) || (g2.name == null) ? "" : g2.name;
        return "MUTEX " + name + " (.r1(" + r1Name + "), .g1(" + g1Name + "), .r2(" + r2Name + "), .g2(" + g2Name + "))";
    }

}
