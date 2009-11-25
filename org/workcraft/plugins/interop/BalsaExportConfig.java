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

package org.workcraft.plugins.interop;

public class BalsaExportConfig {
	public static final BalsaExportConfig DEFAULT = new BalsaExportConfig();

	public enum DummyContractionMode {
		NONE,
		DESIJ,
		PETRIFY
	}

	public enum SynthesisTool {
		MPSAT,
		PETRIFY
	}

	public enum CompositionMode
	{
		DUMMY,
		PCOMP
	}

	private SynthesisTool tool = SynthesisTool.MPSAT;
	private DummyContractionMode dummyContraction = DummyContractionMode.NONE;
	private CompositionMode composition = CompositionMode.DUMMY;

	private BalsaExportConfig()
	{
	}

	public BalsaExportConfig(SynthesisTool tool, DummyContractionMode contraction, CompositionMode composition)
	{
		this.tool = tool;
		this.dummyContraction = contraction;
		this.composition = composition;
	}

	public SynthesisTool synthesisTool() {
		return tool;
	}

	public DummyContractionMode dummyContractionMode() {
		return dummyContraction;
	}

	public CompositionMode compositionMode() {
		return composition;
	}
}
