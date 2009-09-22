/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.modelchecking;

import java.io.File;
import java.io.IOException;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelCheckingFailedException;
import org.workcraft.interop.SynchronousExternalProcess;
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
		if (STG.class.isAssignableFrom(model.getClass()))
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
		/*DotGSerialiser exporter = new DotGSerialiser();
		FileOutputStream ch;
		try {
			ch = new FileOutputStream(new File(tmpNetFilePath));
			exporter.export(model.getMathModel(), ch, null);
			ch.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
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
