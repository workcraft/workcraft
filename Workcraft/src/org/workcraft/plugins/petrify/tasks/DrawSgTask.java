package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;

public class DrawSgTask implements Task<DrawSgResult> {
	private final Model model;
	private Framework framework;
	private boolean writeHuge = false;

	public DrawSgTask(Model model, Framework framework)
	{
		this.model = model;
		this.framework = framework;
	}

	@Override
	public Result<? extends DrawSgResult> run(ProgressMonitor<? super DrawSgResult> monitor)
	{
		try
		{
			File dotG = File.createTempFile("workcraft", ".g");
			dotG.deleteOnExit();

			final Result<? extends Object> dotGResult = framework.getTaskManager().execute(Export.createExportTask(model, dotG, Format.STG, framework.getPluginManager()), "Exporting to .g" );

			if (dotGResult.getOutcome() != Outcome.FINISHED)
			{
				if (dotGResult.getOutcome() != Outcome.CANCELLED)
				{
					if (dotGResult.getCause() != null)
						return Result.failed(dotGResult.getCause());
					else
						return Result.failed(new DrawSgResult(null, "Export to .g failed for unknown reason"));
				}

				return Result.cancelled();
			}

			File sg = File.createTempFile("workcraft", ".g");
			sg.deleteOnExit();

			List<String> writeSgOptions = new ArrayList<String>();

			Result<? extends ExternalProcessResult> writeSgResult;

			while (true)
			{
				writeSgResult = framework.getTaskManager().execute(new WriteSgTask(dotG.getAbsolutePath(), sg.getAbsolutePath(), writeSgOptions), "Running write_sg");

				if (writeSgResult.getOutcome() != Outcome.FINISHED)
				{
					if (writeSgResult.getOutcome() != Outcome.CANCELLED)
					{
						if (writeSgResult.getCause() != null)
							return Result.failed(writeSgResult.getCause());
						else
						{
							final String errorMessages = new String(writeSgResult.getReturnValue().getErrors());

							Pattern p = Pattern.compile("with ([0-9]+) states");
							final Matcher m = p.matcher(errorMessages);
							if (m.find())
							{
								SwingUtilities.invokeAndWait(new Runnable(){
									@Override
									public void run() {
										writeHuge = (JOptionPane
												.showConfirmDialog(
														framework.getMainWindow(),
														"The state graph contains "
														+ m.group(1)
														+ " states. It may take a very long time to be processed. \n\n Are you sure you want to display it?",
														"Please confirm",
														JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
									}
								}
								);

								if (writeHuge)
								{
									writeSgOptions.add("-huge");
									continue;
								} else
								{
									return Result.cancelled();
								}
							} else {
								return Result.failed(new DrawSgResult(null, errorMessages));
							}
						}
					}
					return Result.cancelled();
				} else
					break;
			}

			File ps = File.createTempFile("workcraft", ".ps");
			ps.deleteOnExit();

			final Result<? extends ExternalProcessResult> drawAstgResult = framework.getTaskManager().execute(new DrawAstgTask(sg.getAbsolutePath(), ps.getAbsolutePath(), new ArrayList<String>()), "Running draw_astg");

			if (drawAstgResult.getOutcome() != Outcome.FINISHED)
			{
				if (drawAstgResult.getOutcome() != Outcome.CANCELLED)
				{
					if (drawAstgResult.getCause() != null)
						return Result.failed(drawAstgResult.getCause());
					else
						return Result.failed(new DrawSgResult(null, "Errors running draw_astg: \n" + new String(drawAstgResult.getReturnValue().getErrors())));
				}
				return Result.cancelled();
			}

			dotG.delete();
			sg.delete();

			return Result.finished(new DrawSgResult(ps, "No errors"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return Result.failed(e);
		}
	}
}