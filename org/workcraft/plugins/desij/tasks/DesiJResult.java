package org.workcraft.plugins.desij.tasks;

import java.io.File;
import org.workcraft.dom.Model;

public class DesiJResult {
	private Model specModel;
	private File specificationFile;
	private File[] componentFiles;

	public DesiJResult(Model specModel, File specFile, File[] compFiles) {
		this.specModel = specModel;
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

	public Model getSpecificationModel() {
		return this.specModel;
	}

}
