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

import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Sync;
import org.workcraft.plugins.balsa.handshakeevents.DataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.DataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.SyncStg;
import org.workcraft.plugins.balsa.protocols.ActiveEvent;
import org.workcraft.plugins.balsa.protocols.PassiveEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

public class InternalHandshakeStgBuilder implements HandshakeStgBuilder {

	private final StgBuilder stg;

	public InternalHandshakeStgBuilder(StgBuilder stg) {
		this.stg = stg;
	}

	ActiveEvent active(final InputOutputEvent e)
	{
		return new ActiveEvent(){
			@Override public InputEvent in() { return e; }
			@Override public OutputEvent out() { return e; }};
	}
	PassiveEvent passive(final InputOutputEvent e)
	{
		return new PassiveEvent(){
			@Override public InputEvent in() { return e; }
			@Override public OutputEvent out() { return e; }};
	}
	private PassiveEvent passive() {
		return passive(stg.buildTransition());
	}

	private ActiveEvent active() {
		return active(stg.buildTransition());
	}

	@Override
	public DataPullStg create(PullHandshake handshake) {
		final ActiveEvent go = active();
		final PassiveEvent done = passive();
		final ActiveEvent release = active();
		StgPlace ready = buildPlace(1);
		connect(ready, go.in());
		connect(go.out(), done.in());
		connect(done.out(), release.in());
		connect(release.out(), ready);
		return new DataPullStg() {
			@Override public ActiveEvent go() { return go; }
			@Override public PassiveEvent done() { return done; }
			@Override public ActiveEvent dataRelease() { return release; }
		};
	}

	@Override
	public DataPushStg create(PushHandshake handshake) {
		final ActiveEvent go = active();
		final PassiveEvent done = passive();
		final PassiveEvent release = passive();
		StgPlace ready = buildPlace(1);
		connect(ready, go.in());
		connect(go.out(), release.in());
		connect(release.out(), done.in());
		connect(done.out(), ready);
		return new DataPushStg() {
			@Override public ActiveEvent go() { return go; }
			@Override public PassiveEvent done() { return done; }
			@Override public PassiveEvent dataRelease() { return release; }
		};
	}

	@Override
	public SyncStg create(Sync handshake) {
		final ActiveEvent go = active();
		final PassiveEvent done = passive();
		StgPlace ready = buildPlace(1);
		connect(ready, go.in());
		connect(go.out(), done.in());
		connect(done.out(), ready);
		return new SyncStg() {
			@Override public ActiveEvent go() { return go; }
			@Override public PassiveEvent done() { return done; }
		};
	}

	@Override
	public FullDataPullStg create(FullDataPull handshake) {
		final ActiveEvent go = active();
		StgPlace ready = buildPlace(1);
		connect(ready, go.in());
		StgPlace choice = buildPlace(0);
		connect(go.out(), choice);
		final List<PassiveEvent> results = new ArrayList<PassiveEvent>();
		for(int i=0;i<handshake.getValuesCount();i++)
		{
			PassiveEvent result = passive();
			results.add(result);
			connect(choice, result.in());
			connect(result.out(), ready);
		}

		return new FullDataPullStg() {
			@Override public ActiveEvent go() { return go; }
			@Override public List<PassiveEvent> result() {return results;}
		};
	}

	@Override
	public FullDataPushStg create(FullDataPush handshake) {
		final PassiveEvent done = passive();

		StgPlace ready = buildPlace(1);
		StgPlace received = buildPlace(0);
		connect(done.out(), ready);
		connect(received, done.in());

		final List<ActiveEvent> data = new ArrayList<ActiveEvent>();
		for(int i=0;i<handshake.getValuesCount();i++)
		{
			ActiveEvent result = active();
			data.add(result);
			connect(ready, result.in());
			connect(result.out(), received);
		}

		return new FullDataPushStg() {
			@Override public PassiveEvent done() { return done; }
			@Override public List<ActiveEvent> data() { return data; }
		};
	}

	boolean restrict = false;

	private StgPlace buildPlace(int i) {
		return restrict ? stg.buildPlace(i) : null;
	}

	private void connect(StgPlace place, InputEvent event) {
		if(restrict)
			stg.connect(place, event);
	}

	private void connect(OutputEvent event, StgPlace place) {
		if(restrict)
			stg.connect(event, place);
	}

	private void connect(OutputEvent out, InputEvent in) {
		if(restrict)
			stg.connect(out, in);
	}
}
