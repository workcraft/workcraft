package org.workcraft.plugins.cpog.tools;

import java.util.ArrayList;

import org.workcraft.plugins.cpog.formula.CpogFormula;
import org.workcraft.plugins.cpog.formula.CpogFormulaToString;

public class ArcCondition {

    private final CpogFormula formula;
    private final String boolForm;

    public ArcCondition(CpogFormula formula, String boolForm) {
        this.formula = formula;
        this.boolForm = boolForm;
    }

    public CpogFormula getFormula() {
        return formula;
    }

    public String getBoolForm() {
        return boolForm;
    }

    public ArrayList<String> getVertexList() {
        String f = CpogFormulaToString.toString(formula);
        ArrayList<String> list = new ArrayList<>();
        int index = 0;

        f = f.replace(" ", "");

        while (f.contains("^")) {
            index = f.indexOf("^");
            list.add(f.substring(0, index));
            f = f.substring(index + 1);
        }

        list.add(f);

        return list;

    }

}
