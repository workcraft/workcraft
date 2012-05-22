package org.workcraft.plugins.petri.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;

public class PetriNetSelectionTool extends SelectionTool {

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			if (node != null)
			{
				if(node instanceof VisualPlace)
				{
					VisualPlace place = (VisualPlace) node;
					if (place.getTokens()==1)
						place.setTokens(0);
					else if (place.getTokens()==0)
						place.setTokens(1);
				}
			}

		}
	}

}
