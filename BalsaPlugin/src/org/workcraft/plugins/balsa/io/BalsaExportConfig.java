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

package org.workcraft.plugins.balsa.io;

public class BalsaExportConfig {
	public static final BalsaExportConfig DEFAULT = new BalsaExportConfig();

	public enum CompositionMode
	{
		PCOMP,
		IMPROVED_PCOMP,
		INTERNAL;

		public String toString()
		{
			switch (this) {
			case PCOMP:
				return "Standard parallel composition (PComp)";
			case IMPROVED_PCOMP:
				return "Improved parallel composition (PComp)";
			case INTERNAL:
				return "Event-based composition";
			}

			return "?";
		}
	}

	public enum Protocol
	{
		FOUR_PHASE,
		TWO_PHASE;

		public String toString()
		{
			switch (this) {
			case FOUR_PHASE:
				return "Four phase";
			case TWO_PHASE:
				return "Two phase";
			}
			return "?";
		}
	}

	private SynthesisSettings synthesisSettings = new SynthesisSettings();
	private CompositionMode composition = CompositionMode.PCOMP;
	private Protocol protocol = Protocol.FOUR_PHASE;

	private BalsaExportConfig()
	{
	}

	public BalsaExportConfig(SynthesisSettings synthesisSettings, CompositionMode composition, Protocol protocol)
	{
		this.synthesisSettings = synthesisSettings;
		this.composition = composition;
		this.protocol = protocol;
	}

	public SynthesisSettings getSynthesisSettings()
	{
		return synthesisSettings;
	}

	public CompositionMode getCompositionMode() {
		return composition;
	}

	public Protocol getProtocol() {
		return protocol;
	}
}