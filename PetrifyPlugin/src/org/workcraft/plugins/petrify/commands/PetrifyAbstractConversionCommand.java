package org.workcraft.plugins.petrify.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class PetrifyAbstractConversionCommand extends AbstractConversionCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

    public ArrayList<String> getArgs() {
        return new ArrayList<>();
    }

    public boolean hasSignals(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class) || WorkspaceUtils.isApplicable(we, Fst.class);
    }

    public Collection<Mutex> getMutexes(WorkspaceEntry we) {
        Collection<Mutex> mutexes = null;
        if (WorkspaceUtils.isApplicable(we, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            mutexes = MutexUtils.getMutexes(stg);
        }
        return mutexes;
    }

}
