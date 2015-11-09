/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisTask;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSynthesisResultHandler extends DummyProgressMonitor<MpsatSynthesisChainResult> {
	private String errorMessage;
	private final MpsatSynthesisChainTask task;

	public MpsatSynthesisResultHandler(MpsatSynthesisChainTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends MpsatSynthesisChainResult> result, String description) {
		switch (result.getOutcome()) {
		case FINISHED:
			handleSuccess(result);
			break;
		case FAILED:
			handleFailure(result);
			break;
		default:
			break;
		}
	}

	private void handleSuccess(final Result<? extends MpsatSynthesisChainResult> result) {
		MpsatSynthesisChainResult returnValue = result.getReturnValue();
		final MpsatSynthesisMode mpsatMode = returnValue.getMpsatSettings().getMode();
		ExternalProcessResult mpsatReturnValue = returnValue.getMpsatResult().getReturnValue();
		switch (mpsatMode) {
		case COMPLEX_GATE_IMPLEMENTATION:
			handleSynthesisResult(mpsatReturnValue, false);
			break;
		case GENERALISED_CELEMENT_IMPLEMENTATION:
		case STANDARD_CELEMENT_IMPLEMENTATION:
			handleSynthesisResult(mpsatReturnValue, true);
			break;
		case TECH_MAPPING:
			handleSynthesisResult(mpsatReturnValue, false);
			break;
		default:
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null,
							"MPSat mode \'" + mpsatMode.getArgument() + "\' is not (yet) supported." ,
							"Sorry..", JOptionPane.WARNING_MESSAGE);
				}
			});
			break;
		}
	}

	private void handleFailure(final Result<? extends MpsatSynthesisChainResult> result) {
		MpsatSynthesisChainResult returnValue = result.getReturnValue();
		errorMessage = "MPSat tool chain execution failed :-(";
		Throwable genericCause = result.getCause();
		if (genericCause != null) {
			// Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
			errorMessage += "\n\nFailure caused by: " + genericCause.toString() + "\nPlease see the 'Problems' tab for more details.";
		} else {
			Result<? extends Object> exportResult = returnValue.getExportResult();
			Result<? extends ExternalProcessResult> punfResult = returnValue.getPunfResult();
			Result<? extends ExternalProcessResult> mpsatResult = returnValue.getMpsatResult();
			if (exportResult != null && exportResult.getOutcome() == Outcome.FAILED) {
				errorMessage += "\n\nFailed to export the model as a .g file.";
				Throwable cause = exportResult.getCause();
				if (cause != null) {
					errorMessage += "\n\nFailure caused by: " + cause.toString();
				} else {
					errorMessage += "\n\nThe exporter class did not offer further explanation.";
				}
			} else if (punfResult != null && punfResult.getOutcome() == Outcome.FAILED) {
				errorMessage += "\n\nPunf could not build the unfolding prefix.";
				Throwable cause = punfResult.getCause();
				if (cause != null) {
					errorMessage += "\n\nFailure caused by: " + cause.toString();
				} else {
					errorMessage += "\n\nFailure caused by the following errors:\n" + new String(punfResult.getReturnValue().getErrors());
				}
			} else if (mpsatResult != null && mpsatResult.getOutcome() == Outcome.FAILED) {
				errorMessage += "\n\nMPSat failed to execute as expected.";
				Throwable cause = mpsatResult.getCause();
				if (cause != null) {
					errorMessage += "\n\nFailure caused by: " + cause.toString();
				} else {
					byte[] errors = mpsatResult.getReturnValue().getErrors();
					errorMessage += "\n\nFailure caused by the following errors:\n" + new String(errors);
				}
			} else {
				errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, errorMessage, "Oops..", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private void handleSynthesisResult(ExternalProcessResult mpsatResult, boolean sequentialAssign) {
		final String log = new String(mpsatResult.getOutput());
		if ((log != null) && !log.isEmpty()) {
			System.out.println(log);
			System.out.println();
		}
		byte[] eqnOutput = mpsatResult.getOutputFile(MpsatSynthesisTask.EQN_FILE_NAME);
		if (eqnOutput != null) {
			System.out.println("MPSat synthesis result in EQN format:");
			System.out.println(new String(eqnOutput));
			System.out.println();
		}
		byte[] verilogOutput = mpsatResult.getOutputFile(MpsatSynthesisTask.VERILOG_FILE_NAME);
		if (verilogOutput != null) {
			System.out.println("MPSat synthesis result in Verilog format:");
			System.out.println(new String(verilogOutput));
			System.out.println();
		}
		if (CircuitSettings.getOpenSynthesisResult() && (verilogOutput != null)) {
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(verilogOutput);
				VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
				final Circuit circuit = verilogImporter.importCircuit(in);
				final WorkspaceEntry we = task.getWorkspaceEntry();
				Path<String> path = we.getWorkspacePath();
				final Path<String> directory = path.getParent();
				final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
				final ModelEntry me = new ModelEntry(new CircuitDescriptor() , circuit);
				boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());

				final Framework framework = Framework.getInstance();
				final Workspace workspace = framework.getWorkspace();
				WorkspaceEntry newWorkspaceEntry = workspace.add(directory, name, me, true, openInEditor);
				VisualModel visualModel = newWorkspaceEntry.getModelEntry().getVisualModel();
				if (visualModel instanceof VisualCircuit) {
					VisualCircuit visualCircuit = (VisualCircuit)visualModel;
					visualCircuit.setEnvironmentFile(we.getFile());
					framework.getMainWindow().getCurrentEditor().updatePropertyView();
				}
			} catch (DeserialisationException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
