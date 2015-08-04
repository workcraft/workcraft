package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

	private VisualCPOG visualCpog;
	private WorkspaceEntry we;
	private boolean importAndExtract;

	public PGMinerResultHandler(VisualCPOG visualCpog, WorkspaceEntry we, boolean importAndExtract) {
		this.visualCpog = visualCpog;
		this.we = we;
		this.importAndExtract = importAndExtract;
	}

	public void finished(final Result<? extends ExternalProcessResult> result, String description) {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {


				@Override
				public void run() {
					if (!importAndExtract) {
					final Framework framework = Framework.getInstance();
					CpogDescriptor cpogModel = new CpogDescriptor();
					MathModel mathModel = cpogModel.createMathModel();
					Path<String> path = we.getWorkspacePath();
					VisualModelDescriptor v = cpogModel.getVisualModelDescriptor();
					try {
						if (v == null) {
						throw new VisualModelInstantiationException("visual model is not defined for \"" + cpogModel.getDisplayName() + "\".");
						}
						visualCpog = (VisualCPOG) v.create(mathModel);
						final Workspace workspace = framework.getWorkspace();
						final Path<String> directory = workspace.getPath(we).getParent();
						final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
						final ModelEntry me = new ModelEntry(cpogModel , visualCpog);
						workspace.add(directory, name, me, true, true);


					} catch (VisualModelInstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
					byte[] output = result.getReturnValue().getOutputFile("output.1.cpog");
					String line = "";

					System.out.println("\nResulting Parameterised Graph Equations");
					for (byte b : output) {
						if ((char)b != '\n') {
							line = line + (char)b;
						} else {
							line = line.replaceAll("\r", "");
							System.out.println(line);
							visualCpog.getSelectionTool().insertExpression(line, false, false, true, false);
							line = "";
						}
					}

				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

