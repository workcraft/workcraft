package org.workcraft.plugins.balsa;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.edit.tools.ComponentCreationTool;
import org.workcraft.gui.edit.tools.GraphEditorTool;
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

public final class VisualBalsaCircuit extends VisualModel {

	public VisualBalsaCircuit(BalsaCircuit model) throws VisualModelInstantiationException {
		super(model);
	}

	public VisualBalsaCircuit(BalsaCircuit model, Element element) throws VisualModelInstantiationException {
		super(model, element);
	}

	GraphEditorTool getComponentTool(final Class<? extends org.workcraft.plugins.balsa.components.Component> balsaClass)
	{
		return new ComponentCreationTool(BreezeComponent.class){
			@Override
			protected void initComponent(Component comp) {
				org.workcraft.plugins.balsa.components.Component instance;
				try {
					instance = balsaClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				((BreezeComponent)comp).setUnderlyingComponent(instance);
			}
			@Override
			public String getName() {
				return balsaClass.getSimpleName();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<GraphEditorTool> getAdditionalTools() {
		ArrayList<GraphEditorTool> tools = super.getAdditionalTools();

		Class<?> [] balsaClasses =
			new Class<?>[]
			{
				While.class,
				Concur.class,
				SequenceOptimised.class,

				Adapt.class,
				ActiveEagerFalseVariable.class,
				Arbiter.class,
				Bar.class,
				BinaryFunc.class,
				BinaryFuncConstR.class,
				BuiltinVariable.class,
				Call.class,
				CallMux.class,
				CallDemux.class,
				CallActive.class,
				CallDemuxPush.class,
				Case.class,
				CaseFetch.class,
				Combine.class,
				CombineEqual.class,
				Constant.class,
				Continue.class,
				ContinuePush.class,
				DecisionWait.class,
				Encode.class,
				FalseVariable.class,
				Fetch.class,
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
				Variable.class,
				WireFork.class
			};

		for(Class<?> c : balsaClasses)
			tools.add(getComponentTool((Class<? extends org.workcraft.plugins.balsa.components.Component>) c));

		return tools;
	}
}
