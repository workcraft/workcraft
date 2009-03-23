package org.workcraft.plugins.balsa.handshakes;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainHandshakeMaker {
	static Map<Class<? extends Component>, HandshakeMaker<?>> map = getMap();

	public static Map<String, Handshake> getHandshakes(Component component)
	{
		HandshakeMaker<?> maker = map.get(component.getClass());
		if(maker == null)
			return  new HashMap<String, Handshake>();

		return maker.getComponentHandshakes(component);
	}

	private static Map<Class<? extends Component>, HandshakeMaker<?>> getMap() {
		HashMap<Class<? extends Component>, HandshakeMaker<?>> map = new HashMap<Class<? extends Component>, HandshakeMaker<?>>();

		map.put(org.workcraft.plugins.balsa.components.ActiveEagerFalseVariable.class, new ActiveEagerFalseVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Adapt.class, new AdaptHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Arbiter.class, new ArbiterHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Bar.class, new BarHandshakes());
		map.put(org.workcraft.plugins.balsa.components.BinaryFunc.class, new BinaryFuncHandshakes());
		map.put(org.workcraft.plugins.balsa.components.BinaryFuncConstR.class, new BinaryFuncConstRHandshakes());
		map.put(org.workcraft.plugins.balsa.components.BuiltinVariable.class, new BuiltinVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Call.class, new CallHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CallMux.class, new CallMuxHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CallDemux.class, new CallDemuxHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CallActive.class, new CallActiveHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CallDemuxPush.class, new CallDemuxPushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Case.class, new CaseHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CaseFetch.class, new CaseFetchHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Combine.class, new CombineHandshakes());
		map.put(org.workcraft.plugins.balsa.components.CombineEqual.class, new CombineEqualHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Concur.class, new ConcurHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Constant.class, new ConstantHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Continue.class, new ContinueHandshakes());
		map.put(org.workcraft.plugins.balsa.components.ContinuePush.class, new ContinuePushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.DecisionWait.class, new DecisionWaitHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Encode.class, new EncodeHandshakes());
		map.put(org.workcraft.plugins.balsa.components.FalseVariable.class, new FalseVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Fetch.class, new FetchHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Fork.class, new ForkHandshakes());
		map.put(org.workcraft.plugins.balsa.components.ForkPush.class, new ForkPushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Halt.class, new HaltHandshakes());
		map.put(org.workcraft.plugins.balsa.components.HaltPush.class, new HaltPushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.InitVariable.class, new InitVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Loop.class, new LoopHandshakes());
		map.put(org.workcraft.plugins.balsa.components.NullAdapt.class, new NullAdaptHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Passivator.class, new PassivatorHandshakes());
		map.put(org.workcraft.plugins.balsa.components.PassivatorPush.class, new PassivatorPushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.PassiveEagerFalseVariable.class, new PassiveEagerFalseVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.PassiveSyncEagerFalseVariable.class, new PassiveSyncEagerFalseVariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.SequenceOptimised.class, new SequenceOptimisedHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Slice.class, new SliceHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Split.class, new SplitHandshakes());
		map.put(org.workcraft.plugins.balsa.components.SplitEqual.class, new SplitEqualHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Synch.class, new SynchHandshakes());
		map.put(org.workcraft.plugins.balsa.components.SynchPull.class, new SynchPullHandshakes());
		map.put(org.workcraft.plugins.balsa.components.SynchPush.class, new SynchPushHandshakes());
		map.put(org.workcraft.plugins.balsa.components.UnaryFunc.class, new UnaryFuncHandshakes());
		map.put(org.workcraft.plugins.balsa.components.Variable.class, new VariableHandshakes());
		map.put(org.workcraft.plugins.balsa.components.While.class, new WhileHandshakes());
		map.put(org.workcraft.plugins.balsa.components.WireFork.class, new WireForkHandshakes());

		return map;
	}
}
