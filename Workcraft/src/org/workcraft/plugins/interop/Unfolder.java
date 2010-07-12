package org.workcraft.plugins.interop;

import java.io.File;
import java.io.IOException;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.PunfTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.tasks.Result.Outcome;

// TODO: Merge with new Punf wrappers and remove this class
public class Unfolder {
	public static void makeUnfolding(TaskManager taskManager, File original, File unfolding) throws IOException
	{
		PunfTask task = new PunfTask(original.getAbsolutePath(), unfolding.getAbsolutePath());

		Result<ExternalProcessResult> res = taskManager.execute(task, "Unfolding the Balsa circuit STG");

		System.out.println("Unfolding output: ");
		System.out.write(res.getReturnValue().getOutput());System.out.println();System.out.println("----------------------------------------");

		byte[] errors = res.getReturnValue().getErrors();

		if(errors.length > 0)
		{
			System.out.println("Unfolding errors stream: ");
			System.out.write(errors);System.out.println();System.out.println("----------------------------------------");
		}

		Outcome outcome = res.getOutcome();
		switch(outcome)
		{
		case CANCELLED:
			throw new RuntimeException("Unfolding operation cancelled by user");
		case FINISHED:
			return;
		case FAILED:
			throw new RuntimeException("Punf failed: " + new String(res.getReturnValue().getErrors()), res.getCause());
		}
	}
}
