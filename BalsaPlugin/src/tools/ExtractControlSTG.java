package tools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.ExtractControlSTGTask;
import org.workcraft.plugins.balsa.io.StgExtractionResult;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ExtractControlSTG implements Tool {

	private final Framework framework;

	public ExtractControlSTG(Framework framework) {
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, BalsaCircuit.class);
	}

	@Override
	public String getSection() {
		return "Synthesis";
	}

	@Override
	public void run(final WorkspaceEntry we) {
		PresetManager<BalsaExportConfig> presetManager = new PresetManager<BalsaExportConfig>(new File("config/balsa_export.xml"), new BalsaConfigSerialiser());

		ExtractControlSTGDialog dialog = new ExtractControlSTGDialog(framework.getMainWindow(), presetManager);
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);

		if (dialog.getModalResult()==1)
		{
			final ExtractControlSTGTask task = new ExtractControlSTGTask(framework, WorkspaceUtils.getAs(we, BalsaCircuit.class), dialog.getSettingsFromControls());
			framework.getTaskManager().queue(task, "Extracting control STG", new DummyProgressMonitor<StgExtractionResult>()
					{
						public void finished(final org.workcraft.tasks.Result<? extends StgExtractionResult> result, String description) {
							try {
								SwingUtilities.invokeAndWait(new Runnable()
								{
									public void run() {

										if(result.getOutcome() == Outcome.FINISHED)
										{

											Path<String> path = we.getWorkspacePath();
											String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

											STGModel model = result.getReturnValue().getResult();
											final WorkspaceEntry resolved = framework.getWorkspace().add(path.getParent(), fileName + "_resolved", model, true);
											framework.getMainWindow().createEditorWindow(resolved);
										}
										else
										{
											if(result.getCause() != null)
												ExceptionDialog.show(framework.getMainWindow(), result.getCause());
											else {
												final ExternalProcessResult pcompResult = result.getReturnValue().getPcompResult();
												if(pcompResult != null)
													JOptionPane.showMessageDialog(framework.getMainWindow(), "Parallel composition failed: \n\n" + new String(pcompResult.getErrors()), "Parallel composition failed", JOptionPane.WARNING_MESSAGE);
											}
										}

									};
								});
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						};
					}
			);
		}
	}

	@Override
	public String getDisplayName() {
		return "Extract control STG";
	}
}
