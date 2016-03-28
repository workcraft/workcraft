package org.workcraft.plugins.son.algorithm;

import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

public class BFSEntireEstimationAlg extends DFSEstimationAlg{

	public BFSEntireEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s) throws AlternativeStructureException {
		super(net, d, g, s);
		// TODO Auto-generated constructor stub
	}

}
