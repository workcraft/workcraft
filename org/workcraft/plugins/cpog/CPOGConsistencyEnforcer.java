package org.workcraft.plugins.cpog;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;

public class CPOGConsistencyEnforcer extends HierarchySupervisor {

	private final VisualCPOG visualCPOG;

	public CPOGConsistencyEnforcer(VisualCPOG visualCPOG)
	{
		this.visualCPOG  = visualCPOG;
	}

	@Override
	public void handleEvent(HierarchyEvent e)
	{
		if (e instanceof NodesAddedEvent)
		{
			updateEncoding();
		}
		else
		if (e instanceof NodesDeletedEvent)
		{
			updateEncoding();
		}
	}

	private void updateEncoding()
	{
		for(VisualCPOGGroup group : visualCPOG.getGroups())
		{
			Encoding oldEncoding = group.getEncoding();
			Encoding newEncoding = new Encoding();

			for(VisualVariable var : visualCPOG.getVariables())
			{
				Variable mathVariable = var.getMathVariable();
				newEncoding.setState(mathVariable, oldEncoding.getState(mathVariable));
			}

			group.setEncoding(newEncoding);
		}

	}

}
