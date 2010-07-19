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

import org.workcraft.Framework;
import org.workcraft.FrameworkConsumer;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.shared.MpsatMode;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.MpsatTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;

public class CSCResolver implements Exporter, FrameworkConsumer {

	private TaskManager taskManager;

	@Override
	public void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {
		File tmp = File.createTempFile("toResolve", ".g");
		File mci = File.createTempFile("toResolve", ".mci");
		File output = File.createTempFile("resolved", ".g");
		Export.exportToFile(new DotGExporter(), model, tmp);
		Unfolder.makeUnfolding(taskManager, tmp, mci);
		resolveConflicts(taskManager, mci, null, output);
		FileUtils.copyFileToStream(output, out);
	}

	//private static String mpsatArgsFormat = "-R -f -$1 -p0 -@ -cl";

	public static void resolveConflicts(TaskManager taskManager, File unfolding, File cscResolvedMci, File cscResolvedG) throws IOException
	{
		MpsatSettings settings = new MpsatSettings(MpsatMode.RESOLVE_ENCODING_CONFLICTS, 4, MpsatSettings.SOLVER_MINISAT, SolutionMode.MINIMUM_COST, 1, null);

		Result<? extends ExternalProcessResult> result = taskManager.execute(new MpsatTask(settings.getMpsatArguments(), unfolding.getAbsolutePath()), "CSC conflict resolution");

		System.out.println("MPSAT CSC resolution output: ");
		System.out.write(result.getReturnValue().getOutput());System.out.println();System.out.println("----------------------------------------");
		byte[] errors = result.getReturnValue().getErrors();

		if(errors.length != 0)
		{
			System.out.println("MPSAT CSC resolution errors: ");
			System.out.write(errors);System.out.println();System.out.println("----------------------------------------");
		}

		Outcome outcome = result.getOutcome();

		if(new String(result.getReturnValue().getErrors()).contains("Warning: failed to resolve some of the encoding conflicts"))
			outcome = Outcome.FAILED;

		switch(outcome)
		{
		case CANCELLED:
			throw new RuntimeException("CSC resolution cancelled by user.");
		case FAILED:
			throw new RuntimeException("CSC resolution by MPSat failed: " + new String(errors), result.getCause());
		case FINISHED:
			{
				if(cscResolvedMci != null)
					FileUtils.writeAllBytes(result.getReturnValue().getOutputFile("mpsat.mci"), cscResolvedMci);
				if(cscResolvedG != null)
					FileUtils.writeAllBytes(result.getReturnValue().getOutputFile("mpsat.g"), cscResolvedG);
			}
		}
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

	@Override
	public void acceptFramework(Framework framework) {
		this.taskManager = framework.getTaskManager();
	}
}
