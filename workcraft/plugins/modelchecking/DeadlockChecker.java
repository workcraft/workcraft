package org.workcraft.plugins.modelchecking;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.exceptions.ModelCheckingFailedException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.stg.STG;

public class DeadlockChecker implements ModelChecker
{
	private static final String tmpNetFilePath = "tmp.g";
	private static final String tmpUnfoldingFilePath = "tmp.mci";
	private static final String reachFilePath = "deadlock.re";
	private static final String punfPath = ".\\punf";
	private static final String mpsatPath = ".\\mpsat";


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
		exporter.exportToFile(model, new File(tmpNetFilePath));
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

			ByteBuffer buf = p.getOutputData();

			if(p.getReturnCode()!=0)
			{
				System.out.println("[punf] Unfolding failed.");

				buf.rewind();
				System.out.println(new String (buf.array()));
				cleanUp();
				return;
			}

			System.out.println ("[mpsat] Reachability analysis of " + tmpUnfoldingFilePath + "...");

			SynchronousExternalProcess q = new SynchronousExternalProcess(
					new String[] {mpsatPath, "-F", "-d", "@" + reachFilePath, tmpUnfoldingFilePath}, ".");

			q.start(10000);

			buf = q.getOutputData();
			buf.rewind();

			System.out.println(new String (buf.array()));

			buf = q.getErrorData();
			buf.rewind();

			System.out.println(new String (buf.array()));
			cleanUp();

		} catch(IOException e)
		{
			cleanUp();
			throw new ModelCheckingFailedException(e.getMessage());
		}
	}
}
