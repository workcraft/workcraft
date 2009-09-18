package org.workcraft.serialisation;

import org.workcraft.dom.Model;

public class DeserialisationResult {
	public Model model;
	public ReferenceResolver referenceResolver;

	public DeserialisationResult(Model model, ReferenceResolver referenceResolver) {
		this.model = model;
		this.referenceResolver = referenceResolver;
	}
}
