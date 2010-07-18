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
import org.workcraft.plugins.balsa.stgbuilder.PassiveSignal;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;

public interface StgBuilderForHandshakes
{
	ActiveSignal buildActiveSignal(SignalId signalId);
	PassiveSignal buildPassiveSignal(SignalId signalId);
	ActiveState buildActivePlace(int tokens);
	PassiveState buildPassivePlace(int tokens);
	void connect(ActiveState state, ActiveOut event);
	void connect(PassiveState state, PassiveOut event);
	void connect(PassiveIn reason, PassiveOut consequence);
	void connect(ActiveIn reason, ActiveOut consequence);
	void connect(ActiveIn event, ActiveState state);
	void connect(PassiveIn event, PassiveState state);
	ActiveEvent get(ActiveOut start, PassiveIn end);
	PassiveEvent get(PassiveOut start, ActiveIn end);
	ActiveDummy buildActiveTransition();
	PassiveDummy buildPassiveTransition();
}
