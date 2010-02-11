package org.workcraft.plugins.verification.tasks;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export.ExportTask;

public class MpsatVerificationTask implements Task {
	private Model model;
	private Exporter exporter;
	private String[] mpsatArgs;
	private ProgressMonitor monitor;
	private Framework framework;


	public MpsatVerificationTask(Model model, String[] mpsatArgs, Exporter exporter, Framework framework) {
		this.model = model;
		this.mpsatArgs = mpsatArgs;
		this.exporter = exporter;
		this.framework = framework;
	}

	@Override
	public Result run(ProgressMonitor monitor) {
		this.monitor = monitor;
		try {
			File netFile = File.createTempFile("net", exporter.getExtenstion());
			ExportTask exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());

			framework.getTaskManager().execute(exportTask, "Verification: exporting net");
			monitor.progressUpdate(0.33);
			File mciFile = File.createTempFile("unfolding", ".mci");

			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), mciFile.getCanonicalPath());
			framework.getTaskManager().execute(punfTask, "Verification: unfolding net");

			netFile.delete();

			monitor.progressUpdate(0.66);

			MpsatTask mpsatTask = new MpsatTask(mpsatArgs, mciFile.getCanonicalPath());
			framework.getTaskManager().execute(mpsatTask, "Verification: model-checking");

			monitor.progressUpdate(1.0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return Result.OK;
	}
}