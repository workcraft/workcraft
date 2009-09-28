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
	public static StgInterface active(TwoWayStg stg)
	{
		if(stg instanceof SyncStg)
			return active((SyncStg)stg);
		if(stg instanceof FullDataPullStg)
			return active((FullDataPullStg)stg);
		if(stg instanceof FullDataPushStg)
			return active((FullDataPushStg)stg);
		if(stg instanceof DataPullStg)
			return active((DataPullStg)stg);
		if(stg instanceof DataPushStg)
			return active((DataPushStg)stg);
		throw new RuntimeException("Not supported stg");
	}
	public static StgInterface passive(TwoWayStg stg)
	{
		if(stg instanceof SyncStg)
			return passive((SyncStg)stg);
		if(stg instanceof FullDataPullStg)
			return passive((FullDataPullStg)stg);
		if(stg instanceof FullDataPushStg)
			return passive((FullDataPushStg)stg);
		if(stg instanceof DataPullStg)
			return passive((DataPullStg)stg);
		if(stg instanceof DataPushStg)
			return passive((DataPushStg)stg);
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
		return new ActiveSync()
		{
			@Override
			public OutputEvent go() {
				return active(sync.go());
			}

			@Override
			public InputEvent done() {
				return active(sync.done());
			}
		};
	}
	public static ActiveFullDataPullStg active(final FullDataPullStg sync)
	{
		return new ActiveFullDataPullStg()
		{
			@Override
			public OutputEvent go() {
				return active(sync.go());
			}

			@Override
			public List<InputEvent> result() {
				return active(sync.result());
			}
		};
	}
	public static ActiveFullDataPushStg active(final FullDataPushStg sync)
	{
		return new ActiveFullDataPushStg()
		{
			@Override
			public InputEvent done() {
				return active(sync.done());
			}

			@Override
			public List<OutputEvent> data() {
				return activeA(sync.data());
			}
		};
	}

	public static ActivePushStg active(final DataPushStg sync)
	{
		return new ActivePushStg()
		{
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
		};
	}

	public static ActivePullStg active(final DataPullStg sync)
	{
		return new ActivePullStg()
		{
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
		};
	}


	public static PassiveSync passive(final SyncStg sync)
	{
		return new PassiveSync()
		{
			@Override
			public InputEvent go() {
				return passive(sync.go());
			}

			@Override
			public OutputEvent done() {
				return passive(sync.done());
			}
		};
	}
	public static PassiveFullDataPullStg passive(final FullDataPullStg sync)
	{
		return new PassiveFullDataPullStg()
		{
			@Override
			public InputEvent go() {
				return passive(sync.go());
			}

			@Override
			public List<OutputEvent> result() {
				return passive(sync.result());
			}
		};
	}
	public static PassiveFullDataPushStg passive(final FullDataPushStg sync)
	{
		return new PassiveFullDataPushStg()
		{
			@Override
			public OutputEvent done() {
				return passive(sync.done());
			}

			@Override
			public List<InputEvent> data() {
				return passiveA(sync.data());
			}
		};
	}

	public static PassivePushStg passive(final DataPushStg sync)
	{
		return new PassivePushStg()
		{
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
		};
	}

	public static PassivePullStg passive(final DataPullStg sync)
	{
		return new PassivePullStg()
		{
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
		};
	}

}
