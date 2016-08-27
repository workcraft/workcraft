package org.workcraft.plugins.dfs;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractorTool;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.dfs.tools.CheckDataflowDeadlockTool;
import org.workcraft.plugins.dfs.tools.CheckDataflowPersisitencyTool;
import org.workcraft.plugins.dfs.tools.CheckDataflowTool;
import org.workcraft.plugins.dfs.tools.ComponentMergerTool;
import org.workcraft.plugins.dfs.tools.StgGeneratorTool;
import org.workcraft.plugins.dfs.tools.WaggingGeneratorTool;
import org.workcraft.workspace.WorkspaceEntry;

public class DfsModule implements Module {

    private final class WaggingGenerator2WayTool extends WaggingGeneratorTool {
        @Override
        public String getDisplayName() {
            return "2-way wagging";
        }

        @Override
        public int getWayCount() {
            return 2;
        }
    }

    private final class WaggingGenerator3WayTool extends WaggingGeneratorTool {
        @Override
        public String getDisplayName() {
            return "3-way wagging";
        }

        @Override
        public int getWayCount() {
            return 3;
        }
    }

    private final class WaggingGenerator4WayTool extends WaggingGeneratorTool {
        @Override
        public String getDisplayName() {
            return "4-way wagging";
        }

        @Override
        public int getWayCount() {
            return 4;
        }
    }

    private final class DfsContractorTool extends AbstractContractorTool {
        @Override
        public boolean isApplicableTo(WorkspaceEntry we) {
            return we.getModelEntry().getMathModel() instanceof Dfs;
        }
    }

    @Override
    public String getDescription() {
        return "Dataflow Structure";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(Tool.class, StgGeneratorTool.class);

        pm.registerClass(Tool.class, WaggingGeneratorTool.class);
        pm.registerClass(Tool.class, CheckDataflowDeadlockTool.class);
        pm.registerClass(Tool.class, CheckDataflowPersisitencyTool.class);
        pm.registerClass(Tool.class, CheckDataflowTool.class);
        pm.registerClass(Tool.class, ComponentMergerTool.class);

        pm.registerClass(ModelDescriptor.class, DfsDescriptor.class);
        pm.registerClass(Settings.class, DfsSettings.class);

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new WaggingGenerator2WayTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new WaggingGenerator3WayTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new WaggingGenerator4WayTool();
            }
        });

        pm.registerClass(Tool.class, new Initialiser<Tool>() {
            @Override
            public Tool create() {
                return new DfsContractorTool();
            }
        });
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsDescriptor\"/>");
    }

}
