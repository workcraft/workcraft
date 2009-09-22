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

package org.workcraft.plugins.balsa.stgbuilder;

public interface StgBuilder
{
	StgPlace buildPlace();
	StgPlace buildPlace(int tokenCount);
	StgTransition buildTransition();
	StgSignal buildSignal(SignalId id, boolean isOutput);
	void addConnection(StgPlace place, StgTransition transition);
	void addConnection(StgTransition transition, StgPlace place);
	void addConnection(TransitionOutput transition, StgPlace place);
	void addReadArc(ReadablePlace place, StgTransition transition);
	void addConnection(TransitionOutput t1, StgTransition t2);
}
