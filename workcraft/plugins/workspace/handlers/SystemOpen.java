package org.workcraft.plugins.workspace.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.workcraft.dom.DisplayName;
import org.workcraft.workspace.FileHandler;

@DisplayName("Open")
public class SystemOpen implements FileHandler {


	public boolean accept(File f) {
		if (!f.getName().endsWith(".work"))
			return true;
		else
			return false;
	}


	public void execute(File f) {
		try {
			if (System.getProperty ("os.name").contains("Windows"))
				Runtime.getRuntime().exec (new String[] {"cmd", "/c", f.getAbsolutePath() });
			else
				Desktop.getDesktop().open(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
