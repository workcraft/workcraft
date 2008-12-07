package org.workcraft.framework.workspace;

import java.io.File;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;

public class WorkspaceEntry {
	protected File file;
	protected Model model;
	protected VisualModel visualModel;
	protected String modelTitle;
	protected String modelType;
	protected String modelClassName;


	public WorkspaceEntry() {
		this.file = null;
		this.model = null;
		this.visualModel = null;
		this.modelTitle = null;
		this.modelType = null;
		this.modelClassName = null;
	}

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

	public Model getModel() {
		return model;
	}

	public VisualModel getVisualModel() {
		return visualModel;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public void setVisualModel(VisualModel model) {
		this.visualModel = model;
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

	public boolean isWork() {
		return (model!=null || (file!= null && modelType != null));
	}

//	public boolean isVisualWork() {
	//	return (visualModel != null) ||
	//}


//	public boolean

	public String toString() {
		if (model == null & visualModel == null) {
			// not a workcraft model
		}


		if (model != null) {
			// is a workcraft model
			if (file != null) {
				// has a an associated file
				if (model.getTitle().length()>0)
					return model.getTitle();
				else
					return file.getName();
			} else {
				// does not have a file
				if (model.getTitle().length()>0)
					return model.getTitle() + " - unsaved";
				else
					return "(unnamed) - unsaved";
			}
		} 	else
		// not a workcraft model
			if (file != null)
				return file.getName();
			else
				return "(unnamed)";
	}
}
