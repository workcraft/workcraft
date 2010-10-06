package org.workcraft.workspace;

import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;

public class ModelObject {
	private final ModelDescriptor descriptor;
	private Model model;

	public ModelObject(ModelDescriptor descriptor, Model model)
	{
		this.descriptor = descriptor;
		this.model = model;
	}

	public ModelDescriptor getDescriptor() {
		return descriptor;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
}
