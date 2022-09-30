package org.workcraft.plugins.stg;

public class Mutex {

    public enum Protocol {
        LATE("Late"),
        EARLY("Early");

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

    private Protocol protocol;

    public Mutex(String name, Signal r1, Signal g1, Signal r2, Signal g2) {
        this.name = name;
        this.r1 = r1;
        this.g1 = g1;
        this.r2 = r2;
        this.g2 = g2;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
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
