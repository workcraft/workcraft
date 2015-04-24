package org.workcraft.plugins.circuit.tools;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitStgUtils {

	public static CircuitToStgConverter createCircuitToStgConverter(VisualCircuit circuit) {
		CircuitToStgConverter generator = new CircuitToStgConverter(circuit);
		File envFile = circuit.getEnvironmentFile();
		if ((envFile != null) && envFile.exists()) {
			VisualSTG devStg = generator.getStg();
			VisualSTG systemStg = composeDevStgWithEvnFile(devStg, envFile);
			if (systemStg != null) {
				generator = new CircuitToStgConverter(circuit, systemStg);
			}
		}
		return generator;
	}

	public static VisualSTG composeDevStgWithEvnFile(VisualSTG devStg, File envFile) {
		VisualSTG resultStg = null;
		Framework framework = Framework.getInstance();
		File workingDirectory = FileUtils.createTempDirectory("workcraft-");
		try {
			File devStgFile = exportDevStg(devStg, workingDirectory);
			File envStgFile = exportEnvStg(envFile, workingDirectory);
			File stgFile = composeDevStgWithEnvStg(devStgFile, envStgFile, workingDirectory);
			if (stgFile != null) {
				WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(stgFile, true);
				STG stg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
				resultStg = new VisualSTG(stg);
			}
		} catch (Throwable e) {
		} finally {
			if ((workingDirectory != null) && !MpsatUtilitySettings.getDebugTemporaryFiles()) {
				FileUtils.deleteDirectoryTree(workingDirectory);
			}
		}
		return resultStg;
	}

	private static File composeDevStgWithEnvStg(File devStgFile, File envStgFile, File workingDirectory) throws IOException {
		File stgFile = null;
		Framework framework = Framework.getInstance();
		if ((devStgFile != null) && (envStgFile != null)) {
			// Generating .g for the whole system (circuit and environment)
			stgFile = new File(workingDirectory, "system.g");
			PcompTask pcompTask = new PcompTask(new File[]{devStgFile, envStgFile},
					ConversionMode.OUTPUT, true, false, workingDirectory);

			Result<? extends ExternalProcessResult>  pcompResult = framework.getTaskManager().execute(
					pcompTask, "Running pcomp", null);

			if (pcompResult.getOutcome() == Outcome.FINISHED) {
				FileUtils.writeAllText(stgFile, new String(pcompResult.getReturnValue().getOutput()));
			} else {
				stgFile = null;
			}
		}
		return stgFile;
	}

	private static File exportEnvStg(File envFile, File workingDirectory) throws DeserialisationException, IOException {
		Framework framework = Framework.getInstance();
		File envStgFile = null;
		if (envFile.getName().endsWith(".g")) {
			envStgFile = envFile;
		} else {
			STG envStg = (STG)framework.loadFile(envFile).getMathModel();
			Exporter envStgExporter = Export.chooseBestExporter(framework.getPluginManager(), envStg, Format.STG);
			envStgFile = new File(workingDirectory, "env.g");
			ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getCanonicalPath());
			Result<? extends Object> envExportResult = framework.getTaskManager().execute(
					envExportTask, "Exporting environment .g", null);
			if (envExportResult.getOutcome() != Outcome.FINISHED) {
				envStgFile = null;
			}
		}
		return envStgFile;
	}

	private static File exportDevStg(VisualSTG visualStg, File workingDirectory) throws IOException {
		Framework framework = Framework.getInstance();

		STG devStg = (STG)visualStg.getMathModel();
		Exporter devStgExporter = Export.chooseBestExporter(framework.getPluginManager(), devStg, Format.STG);
		if (devStgExporter == null) {
			throw new RuntimeException ("Exporter not available: model class " + devStg.getClass().getName() + " to format STG.");
		}

		String devStgName = "dev.g";
		File devStgFile =  new File(workingDirectory, devStgName);
		ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getCanonicalPath());
		Result<? extends Object> devExportResult = framework.getTaskManager().execute(
				devExportTask, "Exporting circuit .g", null);
		if (devExportResult.getOutcome() != Outcome.FINISHED) {
			devStgFile = null;
		}

		return devStgFile;
	}

}
