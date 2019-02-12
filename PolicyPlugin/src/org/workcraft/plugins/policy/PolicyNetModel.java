package org.workcraft.plugins.policy;

import org.workcraft.plugins.petri.PetriNetModel;

import java.util.Collection;

public interface PolicyNetModel extends PetriNetModel {
    Collection<Bundle> getBundles();
}
