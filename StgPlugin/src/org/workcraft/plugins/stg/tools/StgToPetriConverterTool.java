package org.workcraft.plugins.stg.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToPetriConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
            return null;
        } else {
            final MainWindow mainWindow = framework.getMainWindow();
            final WorkspaceEntry we = mainWindow.getCurrentWorkspaceEntry();
            we.captureMemento();
            try {
                final VisualStg stg = WorkspaceUtils.getAs(me, VisualStg.class);
                final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
                final StgToPetriConverter converter = new StgToPetriConverter(stg, petri);
                return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
            } finally {
                we.cancelMemento();
            }
        }
    }

}
