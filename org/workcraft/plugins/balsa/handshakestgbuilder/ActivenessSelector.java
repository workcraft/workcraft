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
package org.workcraft.plugins.balsa.handshakestgbuilder;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.balsa.handshakeevents.DataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.DataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.SyncStg;
import org.workcraft.plugins.balsa.handshakeevents.TwoWayStg;
import org.workcraft.plugins.balsa.protocols.ActiveEvent;
import org.workcraft.plugins.balsa.protocols.PassiveEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public class ActivenessSelector {
	private static final class ActiveSyncImpl implements ActiveSync {
		private final SyncStg sync;

		private ActiveSyncImpl(SyncStg sync) {
			this.sync = sync;
		}

		@Override
		public OutputEvent go() {
			return active(sync.go());
		}

		@Override
		public InputEvent done() {
			return active(sync.done());
		}
	}

	private static final class ActiveFullDataPullStgImpl implements
			ActiveFullDataPullStg {
		private final FullDataPullStg sync;

		private ActiveFullDataPullStgImpl(FullDataPullStg sync) {
			this.sync = sync;
		}

		@Override
		public OutputEvent go() {
			return active(sync.go());
		}

		@Override
		public List<InputEvent> result() {
			return active(sync.result());
		}
	}

	private static final class ActiveFullDataPushStgImpl implements
			ActiveFullDataPushStg {
		private final FullDataPushStg sync;

		private ActiveFullDataPushStgImpl(FullDataPushStg sync) {
			this.sync = sync;
		}

		@Override
		public InputEvent done() {
			return active(sync.done());
		}

		@Override
		public List<OutputEvent> data() {
			return activeA(sync.data());
		}
	}

	private static final class ActivePushStgImpl implements ActivePushStg {
		private final DataPushStg sync;

		private ActivePushStgImpl(DataPushStg sync) {
			this.sync = sync;
		}

		@Override
		public InputEvent done() {
			return active(sync.done());
		}

		@Override
		public OutputEvent go() {
			return active(sync.go());
		}

		@Override
		public InputEvent dataRelease() {
			return active(sync.dataRelease());
		}
	}

	private static final class ActivePullStgImpl implements ActivePullStg {
		private final DataPullStg sync;

		private ActivePullStgImpl(DataPullStg sync) {
			this.sync = sync;
		}

		@Override
		public InputEvent done() {
			return active(sync.done());
		}

		@Override
		public OutputEvent go() {
			return active(sync.go());
		}

		@Override
		public OutputEvent dataRelease() {
			return active(sync.dataRelease());
		}
	}

	private static final class PassiveSyncImpl implements PassiveSync {
		private final SyncStg sync;

		private PassiveSyncImpl(SyncStg sync) {
			this.sync = sync;
		}

		@Override
		public InputEvent go() {
			return passive(sync.go());
		}

		@Override
		public OutputEvent done() {
			return passive(sync.done());
		}
	}

	private static final class PassiveFullDataPullStgImpl implements
			PassiveFullDataPullStg {
		private final FullDataPullStg sync;

		private PassiveFullDataPullStgImpl(FullDataPullStg sync) {
			this.sync = sync;
		}

		@Override
		public InputEvent go() {
			return passive(sync.go());
		}

		@Override
		public List<OutputEvent> result() {
			return passive(sync.result());
		}
	}

	private static final class PassiveFullDataPushStgImpl implements
			PassiveFullDataPushStg {
		private final FullDataPushStg sync;

		private PassiveFullDataPushStgImpl(FullDataPushStg sync) {
			this.sync = sync;
		}

		@Override
		public OutputEvent done() {
			return passive(sync.done());
		}

		@Override
		public List<InputEvent> data() {
			return passiveA(sync.data());
		}
	}

	private static final class PassivePullStgImpl implements PassivePullStg {
		private final DataPullStg sync;

		private PassivePullStgImpl(DataPullStg sync) {
			this.sync = sync;
		}

		@Override
		public OutputEvent done() {
			return passive(sync.done());
		}

		@Override
		public InputEvent go() {
			return passive(sync.go());
		}

		@Override
		public InputEvent dataRelease() {
			return passive(sync.dataRelease());
		}
	}

	private static final class PassivePushStgImpl implements PassivePushStg {
		private final DataPushStg sync;

		private PassivePushStgImpl(DataPushStg sync) {
			this.sync = sync;
		}

		@Override
		public OutputEvent done() {
			return passive(sync.done());
		}

		@Override
		public InputEvent go() {
			return passive(sync.go());
		}

		@Override
		public OutputEvent dataRelease() {
			return passive(sync.dataRelease());
		}
	}

	public static StgInterface active(TwoWayStg stg)
	{
		//TODO: bad casting. I hate the idea to .
		if(stg instanceof FullDataPullStg)
			return active((FullDataPullStg)stg);
		if(stg instanceof FullDataPushStg)
			return active((FullDataPushStg)stg);
		if(stg instanceof DataPullStg)
			return active((DataPullStg)stg);
		if(stg instanceof DataPushStg)
			return active((DataPushStg)stg);
		if(stg instanceof SyncStg)
			return active((SyncStg)stg);
		throw new RuntimeException("Not supported stg");
	}
	public static StgInterface passive(TwoWayStg stg)
	{
		if(stg instanceof FullDataPullStg)
			return passive((FullDataPullStg)stg);
		if(stg instanceof FullDataPushStg)
			return passive((FullDataPushStg)stg);
		if(stg instanceof DataPullStg)
			return passive((DataPullStg)stg);
		if(stg instanceof DataPushStg)
			return passive((DataPushStg)stg);
		if(stg instanceof SyncStg)
			return passive((SyncStg)stg);
		throw new RuntimeException("Not supported stg");
	}

	static OutputEvent active(ActiveEvent event) {
		return event.out();}
	static InputEvent active(PassiveEvent event) {
		return event.in();}
	static InputEvent passive(ActiveEvent event) {
		return event.in();}
	static OutputEvent passive(PassiveEvent event) {
		return event.out();}

	private static List<InputEvent> active(final List<PassiveEvent> events) {
		ArrayList<InputEvent> result = new ArrayList<InputEvent>();

		for(PassiveEvent e : events)
			result.add(active(e));

		return result;
	}
	private static List<OutputEvent> activeA(final List<ActiveEvent> events) {
		ArrayList<OutputEvent> result = new ArrayList<OutputEvent>();

		for(ActiveEvent e : events)
			result.add(active(e));

		return result;
	}

	private static List<InputEvent> passiveA(final List<ActiveEvent> events) {
		ArrayList<InputEvent> result = new ArrayList<InputEvent>();

		for(ActiveEvent e : events)
			result.add(passive(e));

		return result;
	}
	private static List<OutputEvent> passive(final List<PassiveEvent> events) {
		ArrayList<OutputEvent> result = new ArrayList<OutputEvent>();

		for(PassiveEvent e : events)
			result.add(passive(e));

		return result;
	}

	public static ActiveSync active(final SyncStg sync)
	{
		return new ActiveSyncImpl(sync);
	}

	public static ActiveFullDataPullStg active(final FullDataPullStg sync)
	{
		return new ActiveFullDataPullStgImpl(sync);
	}

	public static ActiveFullDataPushStg active(final FullDataPushStg sync)
	{
		return new ActiveFullDataPushStgImpl(sync);
	}

	public static ActivePushStg active(final DataPushStg sync)
	{
		return new ActivePushStgImpl(sync);
	}

	public static ActivePullStg active(final DataPullStg sync)
	{
		return new ActivePullStgImpl(sync);
	}

	public static PassiveSync passive(final SyncStg sync)
	{
		return new PassiveSyncImpl(sync);
	}

	public static PassiveFullDataPullStg passive(final FullDataPullStg sync)
	{
		return new PassiveFullDataPullStgImpl(sync);
	}
	public static PassiveFullDataPushStg passive(final FullDataPushStg sync)
	{
		return new PassiveFullDataPushStgImpl(sync);
	}

	public static PassivePushStg passive(final DataPushStg sync)
	{
		return new PassivePushStgImpl(sync);
	}

	public static PassivePullStg passive(final DataPullStg sync)
	{
		return new PassivePullStgImpl(sync);
	}

}
