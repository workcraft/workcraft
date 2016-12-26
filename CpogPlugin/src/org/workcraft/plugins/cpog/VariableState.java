package org.workcraft.plugins.cpog;

public enum VariableState {
    TRUE('1', "[1] true"),
    FALSE('0', "[0] false"),
    UNDEFINED('?', "[?] undefined");

    public final char value;
    private final String name;

    VariableState(char value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static VariableState fromChar(char c) {
        if (c == TRUE.value) return TRUE;
        if (c == FALSE.value) return FALSE;
        return UNDEFINED;
    }
    public String getValueAsString() {
        return Character.toString(value);
    }

    public boolean matches(VariableState state) {
        if (value == state.value) return true;
        if (value == '?' || state.value == '?') return true;
        return false;
    }

    public VariableState toggle() {
        switch (this) {
        case TRUE: return VariableState.FALSE;
        case FALSE: return VariableState.UNDEFINED;
        default: return VariableState.TRUE;
        }
    }

    public static VariableState fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }
}
