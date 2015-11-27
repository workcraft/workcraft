package org.workcraft.plugins.circuit.stg;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitStgUtils {

	public static CircuitToStgConverter createCircuitToStgConverter(VisualCircuit circuit) {
		CircuitToStgConverter generator = new CircuitToStgConverter(circuit);
		File envFile = circuit.getEnvironmentFile();
		if ((envFile != null) && envFile.exists()) {
			STG devStg = (STG)generator.getStg().getMathModel();
			STG systemStg = createSystemStg(devStg, envFile, circuit.getTitle());
			if (systemStg != null) {
				generator = new CircuitToStgConverter(circuit, new VisualSTG(systemStg));
			}
		}
		return generator;
	}

	private static STG createSystemStg(STG devStg, File envFile, String title) {
		STG systemStg = null;
		String prefix = FileUtils.getTempPrefix(title);
		File directory = FileUtils.createTempDirectory(prefix);
		try {
			File devStgFile = exportDevStg(devStg, directory);
			File envStgFile = exportEnvStg(envFile, directory);
			File compStgFile = createComposedStg(devStgFile, envStgFile, directory);
			if (compStgFile != null) {
				systemStg = importStg(compStgFile);
				Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
				restoreInputSignals(systemStg, inputSignalNames);
			}
		} catch (Throwable e) {
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
		return systemStg;
	}

	private static File exportEnvStg(File envFile, File directory) throws DeserialisationException, IOException {
		File envStgFile = null;
		if (envFile.getName().endsWith(StgUtils.ASTG_FILE_EXT)) {
			envStgFile = envFile;
		} else {
			Framework framework = Framework.getInstance();
			ModelEntry modelEntry = framework.loadFile(envFile);
			STG envStg = (STG)modelEntry.getMathModel();
			envStgFile = exportStg(envStg, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
		}
		return envStgFile;
	}

	private static File exportDevStg(STG devStg, File directory) throws IOException {
		return exportStg(devStg, StgUtils.DEVICE_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
	}

	private static File exportSytemStg(STG systemStg, File directory) throws IOException {
		return exportStg(systemStg, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
	}

	private static File exportStg(STG stg, String fileName, File directory) throws IOException {
		File stgFile = new File(directory, fileName);
		Result<? extends Object> exportResult = exportStg(stg, stgFile, directory, null);

		switch (exportResult.getOutcome()) {
		case FINISHED:
			break;
		case CANCELLED:
			stgFile = null;
			break;
		case FAILED:
			throw new RuntimeException("Export failed for file '" + fileName + "':\n" + exportResult.getCause());
		}
		return stgFile;
	}

	public static Result<? extends Object> exportStg(STG stg, File stgFile, File directory,
			ProgressMonitor<? super MpsatChainResult> monitor) throws IOException {

		Framework framework = Framework.getInstance();
		PluginManager pluginManager = framework.getPluginManager();
		Exporter stgExporter = Export.chooseBestExporter(pluginManager, stg, Format.STG);
		if (stgExporter == null) {
			throw new RuntimeException("Exporter not available: model class " + stg.getClass().getName() + " to .g format.");
		}

		ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getCanonicalPath());
		String description = "Exporting " + stgFile.getAbsolutePath();
		SubtaskMonitor<Object> subtaskMonitor = null;
		if (monitor != null) {
			subtaskMonitor = new SubtaskMonitor<Object>(monitor);
		}
		return framework.getTaskManager().execute(exportTask, description, subtaskMonitor);
	}

	private static File createComposedStg(File devStgFile, File envStgFile, File directory) throws IOException {
		File compStgFile = null;
		if ((devStgFile != null) && (envStgFile != null)) {
			// Generating .g for the whole system (circuit and environment)
			compStgFile = new File(directory, StgUtils.COMPOSITION_FILE_NAME + StgUtils.ASTG_FILE_EXT);
			Result<? extends ExternalProcessResult> pcompResult = composeDevWithEnv(devStgFile, envStgFile, directory, null);

			switch (pcompResult.getOutcome()) {
			case FINISHED:
				FileUtils.writeAllText(compStgFile, new String(pcompResult.getReturnValue().getOutput()));
				break;
			case CANCELLED:
				compStgFile = null;
				break;
			case FAILED:
				throw new RuntimeException("Composition failed:\n" + pcompResult.getCause());
			}
		}
		return compStgFile;
	}

	public static Result<? extends ExternalProcessResult> composeDevWithEnv(File devStgFile, File envStgFile, File directory,
			ProgressMonitor<? super MpsatChainResult> monitor) {
		Framework framework = Framework.getInstance();
		File[] inputFiles = new File[]{devStgFile, envStgFile};
		PcompTask pcompTask = new PcompTask(inputFiles, ConversionMode.OUTPUT, true, false, directory);
		String description = "Running parallel composition [PComp]";
		SubtaskMonitor<Object> subtaskMonitor = null;
		if (monitor != null) {
			subtaskMonitor = new SubtaskMonitor<Object>(monitor);
		}
		return framework.getTaskManager().execute(pcompTask, description, subtaskMonitor);
	}

	public static STG importStg(File stgFile) {
		STG stg = null;
		try {
			Framework framework = Framework.getInstance();
			WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(stgFile, true);
			stg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
			framework.getWorkspace().close(stgWorkspaceEntry);
		} catch (DeserialisationException e) {
		}
		return stg;
	}

	public static void restoreInputSignals(STG stg, Collection<String> inputSignalNames) {
		for (String inputName: inputSignalNames) {
			stg.setSignalType(inputName, Type.INPUT, null);
		}
	}

}
