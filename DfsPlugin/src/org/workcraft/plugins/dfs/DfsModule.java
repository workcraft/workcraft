package org.workcraft.plugins.dfs;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.tools.AbstractContractTransformationCommand;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.interop.VerilogExporter;
import org.workcraft.plugins.dfs.tools.DfsDeadlockVerificationCommand;
import org.workcraft.plugins.dfs.tools.DfsPersisitencyVerificationCommand;
import org.workcraft.plugins.dfs.tools.DfsCombinedVerificationCommand;
import org.workcraft.plugins.dfs.tools.MergeComponentTransformationCommand;
import org.workcraft.plugins.dfs.tools.DfsToStgConversionCommand;
import org.workcraft.plugins.dfs.tools.WaggingGeneratorCommand;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DfsModule implements Module {

    private final class WaggingGenerator2WayCommand extends WaggingGeneratorCommand {
        @Override
        public String getDisplayName() {
            return "2-way wagging";
        }

        @Override
        public int getWayCount() {
            return 2;
        }
    }

    private final class WaggingGenerator3WayCommand extends WaggingGeneratorCommand {
        @Override
        public String getDisplayName() {
            return "3-way wagging";
        }

        @Override
        public int getWayCount() {
            return 3;
        }
    }

    private final class WaggingGenerator4WayCommand extends WaggingGeneratorCommand {
        @Override
        public String getDisplayName() {
            return "4-way wagging";
        }

        @Override
        public int getWayCount() {
            return 4;
        }
    }

    private final class ContractComponentTransformationCommand extends AbstractContractTransformationCommand {
        @Override
        public boolean isApplicableTo(ModelEntry me) {
            return WorkspaceUtils.isApplicable(me, Dfs.class);
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

        pm.registerClass(Command.class, DfsToStgConversionCommand.class);

        pm.registerClass(Command.class, WaggingGeneratorCommand.class);
        pm.registerClass(Command.class, DfsDeadlockVerificationCommand.class);
        pm.registerClass(Command.class, DfsPersisitencyVerificationCommand.class);
        pm.registerClass(Command.class, DfsCombinedVerificationCommand.class);
        pm.registerClass(Command.class, MergeComponentTransformationCommand.class);

        pm.registerClass(ModelDescriptor.class, DfsDescriptor.class);
        pm.registerClass(Settings.class, DfsSettings.class);

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new WaggingGenerator2WayCommand();
            }
        });

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new WaggingGenerator3WayCommand();
            }
        });

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new WaggingGenerator4WayCommand();
            }
        });

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new ContractComponentTransformationCommand();
            }
        });

        pm.registerClass(Exporter.class, VerilogExporter.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsDescriptor\"/>");
    }

}
