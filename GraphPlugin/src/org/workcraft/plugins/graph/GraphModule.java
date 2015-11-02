package org.workcraft.plugins.graph;

import java.util.Set;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphModule implements Module {

	private final class VertexMergerTool extends AbstractMergerTool {
		@Override
		public String getDisplayName() {
			return "Merge selected vertices";
		}

		@Override
		public boolean isApplicableTo(WorkspaceEntry we) {
			return we.getModelEntry().getMathModel() instanceof Graph;
		}

		@Override
		public Set<Class<? extends VisualComponent>> getMergableClasses() {
			Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
			result.add(VisualVertex.class);
			return result;
		}
	}

	@Override
	public String getDescription() {
		return "Directed Graph";
	}

	@Override
	public void init() {
		initPluginManager();
		initCompatibilityManager();
	}

	private void initPluginManager() {
		final Framework framework = Framework.getInstance();
		final PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new AbstractContractorTool() {
					@Override
					public boolean isApplicableTo(WorkspaceEntry we) {
						return we.getModelEntry().getMathModel() instanceof Graph;
					}
				};
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new VertexMergerTool();
			}
		});

		pm.registerClass(ModelDescriptor.class, GraphDescriptor.class);
	}

	private void initCompatibilityManager() {
		final Framework framework = Framework.getInstance();
		final CompatibilityManager cm = framework.getCompatibilityManager();

		cm.registerMetaReplacement(
				"<descriptor class=\"org.workcraft.plugins.graph.GraphModelDescriptor\"/>",
				"<descriptor class=\"org.workcraft.plugins.graph.GraphDescriptor\"/>");
	}

}
