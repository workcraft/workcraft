package org.workcraft.plugins.cpog.encoding.twohot;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.formula.Literal;

public class TwoHotRange extends ArrayList<Literal> {

    private static final long serialVersionUID = 4084655691544414217L;
    private final List<Literal> thermometer;

    public TwoHotRange(List<Literal> literals, List<Literal> thermometer) {
        super(literals);
        this.thermometer = thermometer;
    }

    public List<Literal> getThermometer() {
        return thermometer;
    }

}
