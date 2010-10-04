package org.workcraft.plugins.cpog;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;

public class CpogModule implements Module {
	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>() {
			@Override
			public ModelDescriptor create() {
				return new ModelDescriptor() {
					@Override
					public org.workcraft.dom.math.MathModel createMathModel() {
						return new CPOG();
					}

					@Override
					public String getDisplayName() {
						return "Conditional Partial Order Graph";
					}

					@Override
					public VisualModelDescriptor getVisualModelDescriptor() {
						return null;
					}
				};
			}
		});
	}

}
