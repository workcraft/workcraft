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
package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.stgbuilder.ActiveSignal;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.PassiveSignal;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class StgBuilderForHandshakesImpl implements StgBuilderForHandshakes {

	private static class ActiveTransitionImpl implements ActiveDummy
	{
		private final InputOutputEvent transition;

		public ActiveTransitionImpl(InputOutputEvent buildTransition) {
			this.transition = buildTransition;
		}

		@Override public InputEvent in() {
			return transition;
		}

		@Override public OutputEvent out() {
			return transition;
		}
	}

	private static class PassiveTransitionImpl implements PassiveDummy
	{
		private final InputOutputEvent transition;

		public PassiveTransitionImpl(InputOutputEvent buildTransition) {
			this.transition = buildTransition;
		}

		@Override public InputEvent in() {
			return transition;
		}

		@Override public OutputEvent out() {
			return transition;
		}
	}

	private static class ActiveStateImpl implements ActiveState
	{
		public final StgPlace underlying;
		public ActiveStateImpl(StgPlace underlying)
		{
			this.underlying = underlying;
		};
	}

	private static class PassiveStateImpl implements PassiveState
	{
		public final StgPlace underlying;
		public PassiveStateImpl(StgPlace underlying)
		{
			this.underlying = underlying;
		};
	}

	private static class ActiveSignalTransitionImpl implements ActiveSignalTransition
	{
		public final InputOutputEvent underlying;
		public ActiveSignalTransitionImpl(InputOutputEvent event)
		{
			underlying = event;
		}
		@Override public InputEvent in() {
			return underlying;
		}
		@Override public OutputEvent out() {
			return underlying;
		}
	}

	private static class PassiveSignalTransitionImpl implements PassiveSignalTransition
	{
		public final InputOutputEvent underlying;
		public PassiveSignalTransitionImpl(InputOutputEvent event)
		{
			underlying = event;
		}
		@Override public InputEvent in() {
			return underlying;
		}
		@Override public OutputEvent out() {
			return underlying;
		}
	}

	private final StgBuilder builder;
	private final boolean activeAsOutputs;
	public StgBuilderForHandshakesImpl(StgBuilder builder, boolean activeAsOutputs)
	{
		this.builder = builder;
		this.activeAsOutputs = activeAsOutputs;
	}

	@Override
	public ActiveState buildActivePlace(int tokens) {
		return new ActiveStateImpl(builder.buildPlace(tokens));
	}
	@Override
	public ActiveSignal buildActiveSignal(SignalId signalId) {
		final StgSignal signal = builder.buildSignal(signalId, activeAsOutputs);
		return new ActiveSignal()
		{
			@Override public ActiveSignalTransition getMinus() {
				return new ActiveSignalTransitionImpl(signal.getMinus());
			}
			@Override public ActiveSignalTransition getPlus() {
				return new ActiveSignalTransitionImpl(signal.getPlus());
			}
		};
	}
	@Override
	public ActiveDummy buildActiveTransition() {
		return new ActiveTransitionImpl(builder.buildTransition());
	}
	@Override
	public PassiveState buildPassivePlace(int tokens) {
		return new PassiveStateImpl(builder.buildPlace(tokens));
	}
	@Override
	public PassiveSignal buildPassiveSignal(SignalId signalId) {
		final StgSignal signal = builder.buildSignal(signalId, activeAsOutputs);
		return new PassiveSignal()
		{
			@Override public PassiveSignalTransition getMinus() {
				return new PassiveSignalTransitionImpl(signal.getMinus());
			}
			@Override public PassiveSignalTransition getPlus() {
				return new PassiveSignalTransitionImpl(signal.getPlus());
			}
		};
	}
	StgPlace extract(ActiveState state)
	{
		return ((ActiveStateImpl)state).underlying;
	}
	StgPlace extract(PassiveState state)
	{
		return ((PassiveStateImpl)state).underlying;
	}

	@Override
	public PassiveDummy buildPassiveTransition() {
		return new PassiveTransitionImpl(builder.buildTransition());
	}
	@Override
	public void connect(ActiveState state, ActiveOut event) {
		builder.connect(extract(state), event.out());
	}
	@Override
	public void connect(PassiveState state, PassiveOut event) {
		builder.connect(extract(state), event.out());
	}
	@Override
	public void connect(PassiveIn reason, PassiveOut consequence) {
		builder.connect(reason.in(), consequence.out());
	}
	@Override
	public void connect(ActiveIn reason, ActiveOut consequence) {
		builder.connect(reason.in(), consequence.out());
	}
	@Override
	public void connect(ActiveIn event, ActiveState state) {
		builder.connect(event.in(), extract(state));
	}
	@Override
	public void connect(PassiveIn event, PassiveState state) {
		builder.connect(event.in(), extract(state));
	}
	@Override
	public ActiveEvent get(final ActiveOut start, final PassiveIn end) {
		return new ActiveEvent()
		{
			@Override
			public OutputEvent out() {
				return start.out();
			}

			@Override
			public InputEvent in() {
				return end.in();
			}
		};
	}
	@Override
	public PassiveEvent get(final PassiveOut start, final ActiveIn end) {
		return new PassiveEvent()
		{
			@Override
			public OutputEvent out() {
				return start.out();
			}

			@Override
			public InputEvent in() {
				return end.in();
			}
		};
	}
}
