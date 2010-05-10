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
import org.workcraft.plugins.balsa.components.ActiveEagerFalseVariable;
import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.components.Arbiter;
import org.workcraft.plugins.balsa.components.Bar;
import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryFuncConstR;
import org.workcraft.plugins.balsa.components.BuiltinVariable;
import org.workcraft.plugins.balsa.components.Call;
import org.workcraft.plugins.balsa.components.CallActive;
import org.workcraft.plugins.balsa.components.CallDemux;
import org.workcraft.plugins.balsa.components.CallDemuxPush;
import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.components.CaseFetch;
import org.workcraft.plugins.balsa.components.Combine;
import org.workcraft.plugins.balsa.components.CombineEqual;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.Constant;
import org.workcraft.plugins.balsa.components.Continue;
import org.workcraft.plugins.balsa.components.ContinuePush;
import org.workcraft.plugins.balsa.components.DecisionWait;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.components.Encode;
import org.workcraft.plugins.balsa.components.FalseVariable;
import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.components.Fork;
import org.workcraft.plugins.balsa.components.ForkPush;
import org.workcraft.plugins.balsa.components.Halt;
import org.workcraft.plugins.balsa.components.HaltPush;
import org.workcraft.plugins.balsa.components.InitVariable;
import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.components.NullAdapt;
import org.workcraft.plugins.balsa.components.Passivator;
import org.workcraft.plugins.balsa.components.PassivatorPush;
import org.workcraft.plugins.balsa.components.PassiveEagerFalseVariable;
import org.workcraft.plugins.balsa.components.PassiveSyncEagerFalseVariable;
import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.components.Slice;
import org.workcraft.plugins.balsa.components.Split;
import org.workcraft.plugins.balsa.components.SplitEqual;
import org.workcraft.plugins.balsa.components.Synch;
import org.workcraft.plugins.balsa.components.SynchPull;
import org.workcraft.plugins.balsa.components.SynchPush;
import org.workcraft.plugins.balsa.components.UnaryFunc;
import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.components.WireFork;

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

		Class<?> [] balsaClasses =
			new Class<?>[]
			{
				BinaryFunc.class,
				CallMux.class,
				Case.class,
				Concur.class,
				Fetch.class,
				SequenceOptimised.class,
				Variable.class,
				While.class,

				Adapt.class,
				ActiveEagerFalseVariable.class,
				Arbiter.class,
				Bar.class,
				BinaryFuncConstR.class,
				BuiltinVariable.class,
				Call.class,
				CallDemux.class,
				CallActive.class,
				CallDemuxPush.class,
				CaseFetch.class,
				Combine.class,
				CombineEqual.class,
				Constant.class,
				Continue.class,
				ContinuePush.class,
				DecisionWait.class,
				Encode.class,
				FalseVariable.class,
				Fork.class,
				ForkPush.class,
				Halt.class,
				HaltPush.class,
				InitVariable.class,
				Loop.class,
				NullAdapt.class,
				Passivator.class,
				PassivatorPush.class,
				PassiveEagerFalseVariable.class,
				PassiveSyncEagerFalseVariable.class,
				Slice.class,
				Split.class,
				SplitEqual.class,
				Synch.class,
				SynchPull.class,
				SynchPush.class,
				UnaryFunc.class,
				WireFork.class
			};

		tools.add(new SelectionTool());
		tools.add(new ConnectionTool());
		//for(Class<?> c : balsaClasses)
		//	tools.add(getComponentTool((Class<? extends org.workcraft.plugins.balsa.components.Component>) c));

		return tools;
	}
}
