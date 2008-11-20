package org.workcraft.framework.workspace;

import java.io.File;
import org.workcraft.dom.AbstractGraphModel;

public class WorkspaceEntry {
	protected File file;
	protected AbstractGraphModel model;
	protected String modelTitle;
	protected String modelType;
	protected String modelClassName;

	public void setModelTitle(String modelTitle) {
		this.modelTitle = modelTitle;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public void setModelClassName(String modelClassName) {
		this.modelClassName = modelClassName;
	}

	public File getFile() {
		return file;
	}

	public AbstractGraphModel getModel() {
		return model;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setModel(AbstractGraphModel model) {
		this.model = model;
	}

	public String getModelTitle() {
		return modelTitle;
	}

	public String getModelType() {
		return modelType;
	}

	public String getModelClassName() {
		return modelClassName;
	}


	public String toString() {
		if (model != null) {
			if (model.getTitle().length()>0)
				return ">" + model.getTitle();
			else
				if (file != null)
					return ">"+file.getName();
				else
					return ">(unnamed)";
		}
		else
			if (file != null)
				return file.getName();
			else
				return "(unnamed)";
	}
}
