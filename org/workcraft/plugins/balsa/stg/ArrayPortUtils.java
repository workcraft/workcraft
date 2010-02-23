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
package org.workcraft.plugins.balsa.stg;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public class ArrayPortUtils {

	private ArrayPortUtils(){}

	public static List<InputEvent> dataRelease(List<PassivePullStg> ports) {
		List<InputEvent> result = new ArrayList<InputEvent>(ports.size());
		for(PassivePullStg port : ports)
			result.add(port.dataRelease());
		return result;
	}

	public static List<InputEvent> go(List<? extends PassiveSync> ports) {
		List<InputEvent> result = new ArrayList<InputEvent>(ports.size());
		for(PassiveSync port : ports)
			result.add(port.go());
		return result;
	}

	public static List<OutputEvent> done(List<? extends PassiveSync> ports) {
		List<OutputEvent> result = new ArrayList<OutputEvent>(ports.size());
		for(PassiveSync port : ports)
			result.add(port.done());
		return result;
	}
}
