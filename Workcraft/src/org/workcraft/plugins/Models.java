package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.stg.STG;

public class Models implements Module {

	public static class StgModelDescriptor implements ModelDescriptor
	{
		@Override
		public String getDisplayName() {
			return "Signal Transition Graph";
		}

		@Override
		public MathModel createMathModel() {
			return new STG();
		}

		@Override
		public VisualModelDescriptor getVisualModelDescriptor() {
			return null;
		}
	}

	public static class PetriModelDescriptor implements ModelDescriptor
	{
		@Override
		public String getDisplayName() {
			return "Petri Net";
		}

		@Override
		public MathModel createMathModel() {
			return new PetriNet();
		}

		@Override
		public VisualModelDescriptor getVisualModelDescriptor() {
			return null;
		}
	}

	public static class GraphModelDescriptor implements ModelDescriptor
	{
		@Override
		public String getDisplayName() {
			return "Directed Graph";
		}

		@Override
		public MathModel createMathModel() {
			return new Graph();
		}

		@Override
		public VisualModelDescriptor getVisualModelDescriptor() {
			return null;
		}
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>(){@Override public ModelDescriptor create() {
			return new StgModelDescriptor(); } });
		p.registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>(){@Override public ModelDescriptor create() {
			return new PetriModelDescriptor(); } });
		p.registerClass(ModelDescriptor.class, new Initialiser<ModelDescriptor>(){@Override public ModelDescriptor create() {
			return new GraphModelDescriptor(); } });
	}

}
