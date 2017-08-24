package org.workcraft.plugins.dfs;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.commands.AbstractContractTransformationCommand;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.commands.DfsCombinedVerificationCommand;
import org.workcraft.plugins.dfs.commands.DfsDeadlockVerificationCommand;
import org.workcraft.plugins.dfs.commands.DfsPersisitencyVerificationCommand;
import org.workcraft.plugins.dfs.commands.DfsToStgConversionCommand;
import org.workcraft.plugins.dfs.commands.MergeComponentTransformationCommand;
import org.workcraft.plugins.dfs.commands.WaggingGeneratorCommand;
import org.workcraft.plugins.dfs.interop.VerilogExporter;
import org.workcraft.workspace.WorkspaceEntry;
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
        public boolean isApplicableTo(WorkspaceEntry we) {
            return WorkspaceUtils.isApplicable(we, Dfs.class);
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
        pm.registerClass(ModelDescriptor.class, DfsDescriptor.class);
        pm.registerClass(Settings.class, DfsSettings.class);

        pm.registerClass(Command.class, DfsToStgConversionCommand.class);

        pm.registerClass(Command.class, WaggingGeneratorCommand.class);
        pm.registerClass(Command.class, DfsDeadlockVerificationCommand.class);
        pm.registerClass(Command.class, DfsPersisitencyVerificationCommand.class);
        pm.registerClass(Command.class, DfsCombinedVerificationCommand.class);
        pm.registerClass(Command.class, MergeComponentTransformationCommand.class);

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
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.dfs.DfsDescriptor\"/>");
    }

}
