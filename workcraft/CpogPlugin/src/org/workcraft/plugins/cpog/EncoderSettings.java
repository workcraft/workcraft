package org.workcraft.plugins.cpog;

public class EncoderSettings {
    public static final String GO_SIGNAL = "GO";
    public static final String DONE_SIGNAL = "DONE";

    public enum GenerationMode {
        OPTIMAL_ENCODING("Simulated annealing"),
        RECURSIVE("Exhaustive search"),
        RANDOM("Random search"),
        SCENCO("Old tool SCENCO"),
        OLD_SYNT("Old synthesise"),
        SEQUENTIAL("Sequential");

        public final String name;

        GenerationMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private int solutionNumber;
    private GenerationMode genMode;
    private boolean verboseMode;
    private boolean customEncMode;

    private int bits;
    private int circuitSize;
    private boolean effort;
    private boolean cpogSize = false;
    private boolean costFunc = false;
    private boolean abcFlag = false;
    private String[] customEnc = {};

    public EncoderSettings(int solutionNumber, GenerationMode genMode, boolean verboseMode, boolean customEncMode) {
        this.solutionNumber = solutionNumber;
        this.genMode = genMode;
        this.verboseMode = verboseMode;
        this.customEncMode = customEncMode;
    }

    public int getCircuitSize() {
        return circuitSize;
    }

    public void setCircuitSize(int circuitSize) {
        this.circuitSize = circuitSize;
    }

    public boolean isCpogSize() {
        return cpogSize;
    }

    public void setCpogSize(boolean cpogSize) {
        this.cpogSize = cpogSize;
    }

    public boolean isAbcFlag() {
        return abcFlag;
    }

    public void setAbcFlag(boolean abcFlag) {
        this.abcFlag = abcFlag;
    }

    public boolean isCostFunc() {
        return costFunc;
    }

    public void setCostFunc(boolean costFunc) {
        this.costFunc = costFunc;
    }

    public boolean isEffort() {
        return effort;
    }

    public void setEffort(boolean effort) {
        this.effort = effort;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getSolutionNumber() {
        return solutionNumber;
    }

    public void setSolutionNumber(int number) {
        solutionNumber = number;
    }

    public void setGenerationModeInt(int index) {
        switch (index) {
        case 0:
            genMode = GenerationMode.OPTIMAL_ENCODING;
            break;
        case 1:
            genMode = GenerationMode.RECURSIVE;
            break;
        case 2:
            genMode = GenerationMode.RANDOM;
            break;
        case 3:
            genMode = GenerationMode.SCENCO;
            break;
        case 4:
            genMode = GenerationMode.OLD_SYNT;
            break;
        case 5:
            genMode = GenerationMode.SEQUENTIAL;
            break;
        default:
            System.out.println("Error.");
            break;
        }
    }

    public GenerationMode getGenMode() {
        return genMode;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
    }

    public boolean isCustomEncMode() {
        return customEncMode;
    }

    public void setCustomEncMode(boolean customEncMode) {
        this.customEncMode = customEncMode;
    }

    public String[] getCustomEnc() {
        return customEnc;
    }

    public void setCustomEnc(String[] customEnc) {
        this.customEnc = customEnc;
    }

}
