package org.workcraft.plugins.pcomp.tasks;

public class PcompParameters {

    public enum SharedSignalMode {
        OUTPUT("Leave as output"),
        INTERNAL("Make internal"),
        DUMMY("Make dummy");

        private final String description;

        SharedSignalMode(String decription) {
            this.description = decription;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private final SharedSignalMode sharedSignalMode;
    private final boolean sharedOutputs;
    private final boolean improvedComposition;

    public PcompParameters(SharedSignalMode sharedSignalMode, boolean sharedOutputs, boolean improvedComposition) {
        this.sharedSignalMode = sharedSignalMode;
        this.sharedOutputs = sharedOutputs;
        this.improvedComposition = improvedComposition;
    }

    public SharedSignalMode getSharedSignalMode() {
        return sharedSignalMode;
    }

    public boolean isSharedOutputs() {
        return sharedOutputs;
    }

    public boolean isImprovedComposition() {
        return improvedComposition;
    }

}
