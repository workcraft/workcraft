/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSynthesisResultHandler extends DummyProgressMonitor<MpsatChainResult> {
	private String errorMessage;
	private final MpsatChainTask task;

	public MpsatSynthesisResultHandler(MpsatChainTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends MpsatChainResult> result, String description) {
		if (result.getOutcome() == Outcome.FINISHED) {
			final MpsatSynthesisMode mpsatMode = result.getReturnValue().getMpsatSettings().getMode();
			switch (mpsatMode) {
			case COMPLEX_GATE_IMPLEMENTATION:
			case GENERALISED_CELEMENT_IMPLEMENTATION:
			case STANDARD_CELEMENT_IMPLEMENTATION:
			case TECH_MAPPING:
				if (result.getOutcome() == Outcome.FAILED) {
					String msg = result.getReturnValue().getMessage();
					final Framework framework = Framework.getInstance();
					JOptionPane.showMessageDialog(framework.getMainWindow(), msg, "Error", JOptionPane.ERROR_MESSAGE);
				} else if (result.getOutcome() == Outcome.FINISHED) {
					final String log = new String(result.getReturnValue().getMpsatResult().getReturnValue().getOutput());
					System.out.println(log);

					String verilog = new String(result.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.v"));
					if (CircuitSettings.getOpenSynthesisResult() && (verilog != null)) {
						try {
							ByteArrayInputStream in = new ByteArrayInputStream(verilog.getBytes());
							final Circuit circuit = new VerilogImporter().importCircuit(in);
							final WorkspaceEntry we = task.getWorkspaceEntry();
							Path<String> path = we.getWorkspacePath();
							final Path<String> directory = path.getParent();
							final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
							final ModelEntry me = new ModelEntry(new CircuitDescriptor() , circuit);
							boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());

							final Framework framework = Framework.getInstance();
							final Workspace workspace = framework.getWorkspace();
							workspace.add(directory, name, me, true, openInEditor);
						} catch (DeserialisationException e) {
							throw new RuntimeException(e);
						}
					}
				}
				break;
			default:
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
								"MPSat mode \"" + mpsatMode.getArgument() + "\" is not (yet) supported." ,
								"Sorry..", JOptionPane.WARNING_MESSAGE);
					}
				});
				break;
			}
		} else if (result.getOutcome() != Outcome.CANCELLED) {
			errorMessage = "MPSat tool chain execution failed :-(";
			Throwable genericCause = result.getCause();
			if (genericCause != null) {
				// Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
				errorMessage += "\n\nFailure caused by: " + genericCause.toString() + "\nPlease see the \"Problems\" tab for more details.";
			} else {
				MpsatChainResult returnValue = result.getReturnValue();
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
	}

}
