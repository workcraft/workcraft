/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.balsa;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.annotations.CustomTools;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.VisualModelInstantiationException;

@CustomTools(VisualBalsaTools.class)
public final class VisualBalsaCircuit extends AbstractVisualModel {
	public VisualBalsaCircuit(BalsaCircuit model) throws VisualModelInstantiationException {
		super(model);

		Map<MathNode, VisualHandshake> visuals = new HashMap<MathNode, VisualHandshake>();

		for(BreezeComponent component : model.getComponents())
		{
			VisualBreezeComponent visual = new VisualBreezeComponent(component);
			add(visual);

			for(VisualHandshake hc : visual.visualHandshakes.values())
				visuals.put(hc.getReferencedComponent(), hc);
		}


		for(MathConnection connection : model.getConnections()) {
			VisualConnection visualConnection = new VisualConnection();

			VisualHandshake first = visuals.get(connection.getFirst());
			VisualHandshake second = visuals.get(connection.getSecond());

			visualConnection.setVisualConnectionDependencies(first,
					second, new Polyline(visualConnection), connection);
			//VisualConnection visualConnection = new VisualConnection(connection, visuals.get(connection.getFirst()), visuals.get(connection.getSecond()));
			add(visualConnection);
		}
	}

	@Override
	public void validate() throws ModelValidationException {
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}

	@Override
	public Node getNodeByReference(String reference) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public String getNodeReference(Node node) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
