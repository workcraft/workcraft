package org.workcraft.plugins.desij.tasks;

import java.io.File;

public class DesiJResult {

	private File specificationFile;
	private File[] componentFiles;

	public DesiJResult(File specFile, File[] compFiles) {
		this.specificationFile = specFile;
		this.componentFiles = compFiles;
	}

	public File getSpecificationFile() {
		return this.specificationFile;
	}

	/*
	 * Resulting component files
	 * --> could be null if no decomposition operation was performed
	 */
	public File[] getComponentFiles(){
		return this.componentFiles;
	}

}
