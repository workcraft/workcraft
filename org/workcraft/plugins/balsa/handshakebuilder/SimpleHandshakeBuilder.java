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


public class SimpleHandshakeBuilder implements HandshakeBuilder {

	public static SimpleHandshakeBuilder getInstance()
	{
		return new SimpleHandshakeBuilder();
	}

	@Override public PullHandshake CreateActivePull(final int width) { return createPull(width, true); }
	@Override public PullHandshake CreatePassivePull(final int width) { return createPull(width, false); }

	public PullHandshake createPull(final int width, final boolean isActive) {
		return new PullHandshake(){
			@Override public <T> T accept(HandshakeVisitor<T> v) {
				return v.visit(this);
			}

			@Override public int getWidth() {
				return width;
			}

			@Override public boolean isActive() {
				return isActive;
			}
		};
	}

	@Override public PushHandshake CreateActivePush(final int width) { return createPush(width, true); }
	@Override public PushHandshake CreatePassivePush(final int width) { return createPush(width, false); }

	public PushHandshake createPush(final int width, final boolean isActive) {
		return new PushHandshake()
		{
			@Override public <T> T accept(HandshakeVisitor<T> v) {
				return v.visit(this);
			}

			@Override public int getWidth() {
				return width;
			}

			@Override public boolean isActive() {
				return isActive;
			}
		};
	}

	@Override public Sync CreateActiveSync() { return createSync(true); }
	@Override public Sync CreatePassiveSync() { return createSync(false); }

	private Sync createSync(final boolean isActive) {
		return new Sync()
		{
			public <T> T accept(HandshakeVisitor<T> v) {
				return v.visit(this);
			}

			@Override
			public boolean isActive() {
				return isActive;
			}
		};
	}

	@Override public FullDataPush CreateActiveFullDataPush(int valuesCount) { return createFullDataPush(valuesCount, true); }
	@Override public FullDataPush CreatePassiveFullDataPush(int valuesCount) { return createFullDataPush(valuesCount, false); }

	private FullDataPush createFullDataPush(final int valuesCount, final boolean active) {
		return new FullDataPush()
		{
			@Override public <T> T accept(HandshakeVisitor<T> v) {
				return v.visit(this);
			}

			@Override public boolean isActive() {
				return active;
			}

			@Override public int getValuesCount() {
				return valuesCount;
			}
		};
	}

	@Override public FullDataPull CreateActiveFullDataPull(int valuesCount) { return createFullDataPull(valuesCount, true); }
	@Override public FullDataPull CreatePassiveFullDataPull(int valuesCount) { return createFullDataPull(valuesCount, false); }

	private FullDataPull createFullDataPull(final int valuesCount, final boolean active) {
		return new FullDataPull()
		{
			@Override public <T> T accept(HandshakeVisitor<T> v) {
				return v.visit(this);
			}

			@Override public boolean isActive() {
				return active;
			}

			@Override public int getValuesCount() {
				return valuesCount;
			}
		};
	}
}
