package org.workcraft.plugins.generic;

import org.workcraft.dom.ModelMeta;
import org.workcraft.framework.Model;
import org.workcraft.framework.Plugin;

@ModelMeta (displayName = "Generic model")
public class GenericModel extends Model {
	//public static final String CAPTION = "Generic model";

	public static Plugin getInstance() {
		return new GenericModel();
	}
}
