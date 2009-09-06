package org.workcraft.testing.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.util.Import;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.serialisation.DotGImporter;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Hierarchy;

import com.sun.corba.se.pept.transport.Connection;

public class DotGImporterTests {
	@Test
	public void Test1() throws IOException, DeserialisationException
	{
		File tempFile = File.createTempFile("test", ".g");

		FileOutputStream fileStream = new FileOutputStream(tempFile);

		OutputStreamWriter writer = new OutputStreamWriter(fileStream);

		writer.write("\n");
		writer.write("   #test \n");
		writer.write("   # for DotGImporter\n");
		writer.write("\n");
		writer.write(".outputs  x\t y   z\n");
		writer.write("\n");
		writer.write(".inputs  a\tb \tc\n");
		writer.write("\n");
		writer.write(" \t.graph\n");
		writer.write("a+ p1 p2\n");
		writer.write("b+ p1 p2\n");
		writer.write(" c+  p1 \t p2\n");
		writer.write("\n");
		writer.write("p1 z+ y+ x+\n");
		writer.write("p2 z+ y+ x+\n");
		writer.write("\n");
		writer.write(".marking { }\n");
		writer.write(".end\n");

		writer.close();
		fileStream.close();

		STG imported = (STG) Import.importFromFile(new DotGImporter(), tempFile);

		Assert.assertEquals(6, Hierarchy.getChildrenOfType(imported.getRoot(), Transition.class).size());
		Assert.assertEquals(2, Hierarchy.getChildrenOfType(imported.getRoot(), Place.class).size());
		Assert.assertEquals(12, Hierarchy.getChildrenOfType(imported.getRoot(), Connection.class).size());
	}
}
