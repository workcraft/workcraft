package org.workcraft.plugins.circuit.utils;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.Not;
import org.workcraft.formula.Or;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgSettings;

public class MutexUtils {

    public static BooleanFormula getGrantSet(VisualFunctionContact reqContact,
            VisualFunctionContact otherGrantContact, VisualFunctionContact otherReqContact) {

        return getGrantSet(reqContact.getReferencedComponent(),
                otherGrantContact.getReferencedComponent(), otherReqContact.getReferencedComponent());
    }

    public static BooleanFormula getGrantSet(BooleanFormula reqContact,
            BooleanFormula otherGrantContact, BooleanFormula otherReqContact) {

        BooleanFormula result = new And(reqContact, new Not(otherGrantContact));
        if (StgSettings.getMutexProtocol() == Mutex.Protocol.RELAXED) {
            result = new Or(result, new And(reqContact, new Not(otherReqContact)));
        }
        return result;
    }

    public static BooleanFormula getGrantReset(VisualFunctionContact reqContact) {
        return getGrantReset(reqContact.getReferencedComponent());
    }

    public static BooleanFormula getGrantReset(BooleanFormula reqContact) {
        return new Not(reqContact);
    }

}
