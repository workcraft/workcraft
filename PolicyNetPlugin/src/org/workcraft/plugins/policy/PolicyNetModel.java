package org.workcraft.plugins.policy;

import java.util.Collection;

import org.workcraft.plugins.petri.PetriNetModel;

public interface PolicyNetModel extends PetriNetModel {
    Collection<Bundle> getBundles();
}
