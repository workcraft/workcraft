package org.workcraft.plugins.circuit.verilog;

import java.util.Map;

public class SubstitutionRule {
    public final String oldName;
    public final String newName;
    public final Map<String, String> substitutions;
    public final String extras;

    public SubstitutionRule(String oldName, String newName, Map<String, String> substitutions, String extras) {
        this.oldName = oldName;
        this.newName = newName;
        this.substitutions = substitutions;
        this.extras = extras;
    }

}
