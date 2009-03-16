package org.workcraft.testing.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Component;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.STG;

public class DotGImporterTests {
	@Test
	public void Test1() throws IOException
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

		STG imported = (STG)new DotGImporter().importFromFile(tempFile);

		Set<Component> components = imported.getComponents();
		Assert.assertEquals(8, components.size());
		Assert.assertEquals(12, imported.getConnections().size());
	}
}
