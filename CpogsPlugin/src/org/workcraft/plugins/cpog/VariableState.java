/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
        switch(this) {
            case TRUE: return VariableState.FALSE;
            case FALSE: return VariableState.UNDEFINED;
            default: return VariableState.TRUE;
        }
    }

    public static VariableState fromBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }
}
