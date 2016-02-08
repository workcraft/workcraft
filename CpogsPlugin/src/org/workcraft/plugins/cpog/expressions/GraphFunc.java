/* J. R. Beaumont
 * 25/06/2014
 * Class extension of Func, to allow PGs options to remove graph names
 */

package org.workcraft.plugins.cpog.expressions;

import java.util.ArrayList;

import org.workcraft.util.Func;

public interface GraphFunc <Arg, Result> extends Func<Arg, Result>{

    public GraphFunc<Arg, Result> removeGraphName(String graphName);

    CpogFormula eval(String label, String boolExpression) throws org.workcraft.plugins.cpog.expressions.javacc.ParseException;

    public void setSequenceCondition(CpogFormula formula, String boolForm);

//    public boolean getRef();
}
