package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractConversionCommand extends  org.workcraft.commands.AbstractConversionCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
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
