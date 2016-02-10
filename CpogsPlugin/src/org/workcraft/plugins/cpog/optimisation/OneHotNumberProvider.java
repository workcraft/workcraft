package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.One;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

public class OneHotNumberProvider implements NumberProvider<OneHotIntBooleanFormula> {
    private final List<BooleanFormula> rho = new ArrayList<BooleanFormula>();

    public OneHotNumberProvider() {
    }

    @Override
    public OneHotIntBooleanFormula generate(String varPrefix, int range) {
        List<BooleanVariable> vars = new ArrayList<BooleanVariable>();
        for(int i=0;i<range;i++)
            vars.add(new FreeVariable(varPrefix + "sel"+i));

        for(int i=0;i<range;i++)
            for(int j=i+1;j<range;j++)
                rho.add(or(not(vars.get(i)), not(vars.get(j))));

        rho.add(or(vars));

        return new OneHotIntBooleanFormula(vars);
    }

    @Override
    public BooleanFormula select(BooleanFormula[] booleanFormulas,
            OneHotIntBooleanFormula number) {
        List<BooleanFormula> conditions = new ArrayList<BooleanFormula>();

        if(number.getRange() != booleanFormulas.length)
            throw new RuntimeException("Lengths do not match");

        for(int i=0;i<booleanFormulas.length;i++)
            conditions.add(imply(number.get(i), booleanFormulas[i]));

        return and(conditions);
    }

    @Override
    public BooleanFormula getConstraints() {
        return and(rho);
    }

    @Override
    public BooleanFormula less(OneHotIntBooleanFormula a,
            OneHotIntBooleanFormula b) {
        return One.instance();
    }
}
