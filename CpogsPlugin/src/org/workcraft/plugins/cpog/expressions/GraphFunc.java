/* J. R. Beaumont
 * 25/06/2014
 * Class extension of Func, to allow PGs options to remove graph names
 */

package org.workcraft.plugins.cpog.expressions;

import org.workcraft.util.Func;

public interface GraphFunc <Arg, Result> extends Func<Arg, Result>{

    GraphFunc<Arg, Result> removeGraphName(String graphName);

    CpogFormula eval(String label, String boolExpression) throws org.workcraft.plugins.cpog.expressions.jj.ParseException;

    void setSequenceCondition(CpogFormula formula, String boolForm);

//    public boolean getRef();
}
