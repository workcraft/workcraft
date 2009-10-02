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

package org.workcraft.plugins.balsa.handshakebuilder;

public interface HandshakeBuilder
{
	public Sync CreateActiveSync();
	public Sync CreatePassiveSync();
	public PullHandshake CreatePassivePull(int width);
	public PullHandshake CreateActivePull(int width);
	public PushHandshake CreatePassivePush(int width);
	public PushHandshake CreateActivePush(int width);
	public FullDataPull CreateActiveFullDataPull(int width);
	public FullDataPull CreatePassiveFullDataPull(int width);
	public FullDataPush CreateActiveFullDataPush(int width);
	public FullDataPush CreatePassiveFullDataPush(int width);
}
