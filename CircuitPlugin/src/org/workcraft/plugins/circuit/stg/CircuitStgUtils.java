package org.workcraft.plugins.circuit.stg;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitStgUtils {

	public static CircuitToStgConverter createCircuitToStgConverter(VisualCircuit circuit) {
		CircuitToStgConverter generator = new CircuitToStgConverter(circuit);
		File envFile = circuit.getEnvironmentFile();
		if ((envFile != null) && envFile.exists()) {
			STG devStg = (STG)generator.getStg().getMathModel();
			STG systemStg = composeDevStgWithEvnFile(devStg, envFile, circuit.getTitle());
			if (systemStg != null) {
				generator = new CircuitToStgConverter(circuit, new VisualSTG(systemStg));
			}
		}
		return generator;
	}

	public static STG composeDevStgWithEvnFile(STG devStg, File envFile, String title) {
		STG systemStg = null;
		String prefix = FileUtils.getTempPrefix(title);
		File directory = FileUtils.createTempDirectory(prefix);
		try {
			File devStgFile = exportDevStg(devStg, directory);
			File envStgFile = exportEnvStg(envFile, directory);
			File systemStgFile = composeDevStgWithEnvStg(devStgFile, envStgFile, directory);
			if (systemStgFile != null) {
				Framework framework = Framework.getInstance();
				WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(systemStgFile, true);
				systemStg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
				for (String inputName: devStg.getSignalNames(Type.INPUT, null)) {
					systemStg.setSignalType(inputName, Type.INPUT, null);
				}
				framework.getWorkspace().close(stgWorkspaceEntry);
			}
		} catch (Throwable e) {
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
		return systemStg;
	}

	private static File composeDevStgWithEnvStg(File devStgFile, File envStgFile, File directory) throws IOException {
		File stgFile = null;
		Framework framework = Framework.getInstance();
		if ((devStgFile != null) && (envStgFile != null)) {
			// Generating .g for the whole system (circuit and environment)
			stgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT);
			PcompTask pcompTask = new PcompTask(new File[]{devStgFile, envStgFile},
					ConversionMode.OUTPUT, true, false, directory);

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

	private static File exportEnvStg(File envFile, File directory) throws DeserialisationException, IOException {
		Framework framework = Framework.getInstance();
		File envStgFile = null;
		if (envFile.getName().endsWith(StgUtils.ASTG_FILE_EXT)) {
			envStgFile = envFile;
		} else {
			STG envStg = (STG)framework.loadFile(envFile).getMathModel();
			Exporter envStgExporter = Export.chooseBestExporter(framework.getPluginManager(), envStg, Format.STG);
			envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
			ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getCanonicalPath());
			Result<? extends Object> envExportResult = framework.getTaskManager().execute(
					envExportTask, "Exporting environment .g", null);
			if (envExportResult.getOutcome() != Outcome.FINISHED) {
				envStgFile = null;
			}
		}
		return envStgFile;
	}

	private static File exportDevStg(STG devStg, File directory) throws IOException {
		Framework framework = Framework.getInstance();

		Exporter devStgExporter = Export.chooseBestExporter(framework.getPluginManager(), devStg, Format.STG);
		if (devStgExporter == null) {
			throw new RuntimeException("Exporter not available: model class " + devStg.getClass().getName() + " to .g format.");
		}

		File devStgFile =  new File(directory, StgUtils.DEVICE_FILE_NAME + StgUtils.ASTG_FILE_EXT);
		ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getCanonicalPath());
		Result<? extends Object> devExportResult = framework.getTaskManager().execute(
				devExportTask, "Exporting device .g", null);
		if (devExportResult.getOutcome() != Outcome.FINISHED) {
			devStgFile = null;
		}

		return devStgFile;
	}

}
