package org.workcraft.plugins.policy;

import org.workcraft.plugins.petri.PetriModel;

import java.util.Collection;

public interface PolicyModel extends PetriModel {
    Collection<Bundle> getBundles();
}
