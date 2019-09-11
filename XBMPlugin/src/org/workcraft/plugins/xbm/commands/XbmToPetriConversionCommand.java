package org.workcraft.plugins.xbm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPetriDescriptor;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.converters.XbmToPetriConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class XbmToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualXbm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualXbm visualXbm = me.getAs(VisualXbm.class);
        final VisualPetri visualPetri = new VisualPetri(new Petri());
        final XbmToPetriConverter converter = new XbmToPetriConverter(visualXbm, visualPetri);
        return new ModelEntry(new PetriDescriptor(), converter.getDstModel());
    }
}
