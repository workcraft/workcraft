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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.plugins.balsa.io.BalsaToGatesExporter;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;

public class CSCResolver implements Exporter {

	@Override
	public void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {
		File tmp = File.createTempFile("toResolve", ".g");
		File mci = File.createTempFile("toResolve", ".mci");
		File output = File.createTempFile("resolved", ".g");
		Export.exportToFile(new DotGExporter(), model, tmp);
		BalsaToGatesExporter.makeUnfolding(tmp, mci);
		resolveConflicts(mci, null, output);
		FileUtils.copyFileToStream(output, out);
	}

	private static String mpsatArgsFormat = "-R -f -$1 -p0 -@ -cl";

	public static void resolveConflicts(File unfolding, File cscResolvedMci, File cscResolvedG) throws IOException {
		File resolutionDir = FileUtils.createTempDirectory("CSCResolution");

		String[] split = mpsatArgsFormat.split(" ");
		String[] args = new String[split.length + 2];
		args[0] = PetriNetToolsSettings.getMpsatCommand();
		for(int i=0;i<split.length;i++)
			args[i+1] = split[i];
		args[split.length+1] = unfolding.getAbsolutePath();

		SynchronousExternalProcess process = new SynchronousExternalProcess(args, resolutionDir.getAbsolutePath());

		if(!process.start(300000000))
			throw new RuntimeException("MPSAT SCS resolution timed out. \nProcess Output:\n" + new String(process.getErrorData()));
		System.out.println("MPSAT CSC resolution output: ");
		System.out.write(process.getOutputData());System.out.println();System.out.println("----------------------------------------");
		System.out.println("MPSAT CSC resolution errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");

		if(process.getReturnCode() != 0)
			throw new RuntimeException("MPSAT SCS resolution failed: " + new String(process.getErrorData()));

		if(cscResolvedMci != null)
			FileUtils.copyFile(new File(resolutionDir, "mpsat.mci"), cscResolvedMci);
		if(cscResolvedG != null)
			FileUtils.copyFile(new File(resolutionDir, "mpsat.g"), cscResolvedG);

		FileUtils.deleteDirectoryTree(resolutionDir);
	}

	@Override
	public String getDescription() {
		return ".g with resolved CSC conflicts";
	}

	@Override
	public String getExtenstion() {
		return ".g";
	}

	@Override
	public int getCompatibility(Model model) {
		if (model instanceof STG)
			return Exporter.GENERAL_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	@Override
	public UUID getTargetFormat() {
		return Format.STG;
	}
}
