package org.workcraft.plugins.modelchecking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ModelCheckingFailedException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.stg.STG;

public class DeadlockChecker implements ModelChecker
{
	private static final String tmpNetFilePath = "tmp.g";
	private static final String tmpUnfoldingFilePath = "tmp.mci";
	private static final String reachFilePath = "deadlock.re";
	private static final String punfPath = "..//Util/punf";
	private static final String mpsatPath = "..//Util/mpsat";


	public String getDisplayName() {
		return "Deadlock";
	}

	public boolean isApplicableTo(Model model)
	{
		if (STG.class.isAssignableFrom(model.getMathModel().getClass()))
				return true;
		return false;
	}

	private void cleanUp()
	{
		(new File(tmpNetFilePath)).delete();
		(new File(tmpUnfoldingFilePath)).delete();
	}

	private void exportNet(Model model)
	{
		DotGExporter exporter = new DotGExporter();
		WritableByteChannel ch;
		try {
			ch = new FileOutputStream(new File(tmpNetFilePath)).getChannel();
			exporter.export(model, ch);
			ch.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(Model model) throws ModelCheckingFailedException
	{
		try
		{
			System.out.println("Exporting model into " + tmpNetFilePath + "...");

			exportNet(model);

			System.out.println("[punf] Net unfolding into " + tmpUnfoldingFilePath + "...");

			SynchronousExternalProcess p = new SynchronousExternalProcess(
					new String[] {punfPath, "-f=" + tmpNetFilePath,  "-m=" + tmpUnfoldingFilePath}, ".");

			p.start(10000);

			byte [] buf = p.getOutputData();

			if(p.getReturnCode()!=0)
			{
				System.out.println("[punf] Unfolding failed.");

				System.out.println(new String (buf));
				cleanUp();
				return;
			}

			System.out.println ("[mpsat] Reachability analysis of " + tmpUnfoldingFilePath + "...");

			SynchronousExternalProcess q = new SynchronousExternalProcess(
					new String[] {mpsatPath, "-F", "-d", "@" + reachFilePath, tmpUnfoldingFilePath}, ".");

			q.start(10000, new byte[]{13, 13, 13, 10, 13, 10, 13});

			buf = q.getOutputData();

			System.out.println(new String (buf));

			buf = q.getErrorData();

			System.out.println(new String (buf));
			cleanUp();

		} catch(IOException e)
		{
			cleanUp();
			throw new ModelCheckingFailedException(e.getMessage());
		}
	}
}
