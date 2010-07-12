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

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.AbstractNodeGenerator;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.balsa.components.DynamicComponent;

public class VisualBalsaTools implements CustomToolsProvider
{
	GraphEditorTool getComponentTool(final String componentName)
	{
		return new NodeGeneratorTool(new AbstractNodeGenerator(){
			@Override
			protected BreezeComponent createMathNode() {
				BreezeComponent comp = new BreezeComponent();
				DynamicComponent instance = null;
				try {
					//TODO: Instantiate a DynamicComponent
					//instance = new BreezeLibrary(BalsaSystem.DEFAULT()).get(name) balsaClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				comp.setUnderlyingComponent(instance);
				return comp;
			}

			@Override
			public String getLabel() {
				return componentName;
			}
		});
	}

	@Override
	public ArrayList<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> tools = new ArrayList<GraphEditorTool>();

		//TODO
		@SuppressWarnings("unused")
		Class<?> [] balsaClasses =
			new Class<?>[]
			{
			};

		tools.add(new SelectionTool());
		tools.add(new ConnectionTool());
		//for(Class<?> c : balsaClasses)
		//	tools.add(getComponentTool((Class<? extends org.workcraft.plugins.balsa.components.Component>) c));

		return tools;
	}
}
