package org.workcraft.plugins.cpog.tools;

import java.util.ArrayList;

import org.workcraft.plugins.cpog.expressions.CpogFormula;
import org.workcraft.plugins.cpog.expressions.CpogFormulaToString;

public class ArcCondition {

    private CpogFormula formula;
    private String boolForm;

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
        ArrayList<String> list = new ArrayList<String>();
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
