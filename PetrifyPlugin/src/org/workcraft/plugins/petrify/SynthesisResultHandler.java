package org.workcraft.plugins.petrify;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitModelDescriptor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.petrify.tasks.SynthesisResult;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;


public class SynthesisResultHandler extends DummyProgressMonitor<SynthesisResult> {
	private final WorkspaceEntry we;

	public SynthesisResultHandler(WorkspaceEntry we) {
		this.we = we;
	}

	@Override
	public void finished(Result<? extends SynthesisResult> result, String description) {
		if (result.getOutcome() == Outcome.FAILED) {
			String msg = result.getReturnValue().getStderr();
			final Framework framework = Framework.getInstance();
			JOptionPane.showMessageDialog(framework.getMainWindow(), msg, "Error", JOptionPane.ERROR_MESSAGE);
		} else if (result.getOutcome() == Outcome.FINISHED) {

			String log = result.getReturnValue().getLog();
			if (log != null) {
				System.out.println(log);
			}

			String equations = result.getReturnValue().getEquation();
			if (equations != null) {
				System.out.println(equations);
			}

			String verilog = result.getReturnValue().getVerilog();
			if (CircuitSettings.getOpenSynthesisResult() && (verilog != null)) {
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(verilog.getBytes());
					final Circuit circuit = new VerilogImporter().importCircuit(in);
					Path<String> path = we.getWorkspacePath();
					final Path<String> directory = path.getParent();
					final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
					final ModelEntry me = new ModelEntry(new CircuitModelDescriptor() , circuit);
					boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());

					final Framework framework = Framework.getInstance();
					final Workspace workspace = framework.getWorkspace();
					workspace.add(directory, name, me, true, openInEditor);
				} catch (DeserialisationException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
