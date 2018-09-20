package org.workcraft.plugins.wtg.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.converter.WtgToStgConverter;
import org.workcraft.plugins.wtg.utils.VerificationUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class WtgToStgConversionCommand extends AbstractConversionCommand {

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Wtg.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final Wtg wtg = me.getAs(Wtg.class);
        final Stg stg = new Stg();
        ModelEntry result = null;
        if (VerificationUtils.checkStructure(wtg) && VerificationUtils.checkNameCollisions(wtg)) {
            final WtgToStgConverter converter = new WtgToStgConverter(wtg, stg);
            result = new ModelEntry(new StgDescriptor(), converter.getDstModel());
        }
        return result;
    }

}
