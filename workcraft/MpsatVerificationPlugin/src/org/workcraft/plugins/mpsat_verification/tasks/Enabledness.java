package org.workcraft.plugins.mpsat_verification.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Enabledness {

    private final HashSet<String> enabled;
    private final HashSet<String> disabled;

    public Enabledness(Collection<String> enabled, Collection<String> disabled) {
        this.enabled = new HashSet<>(enabled);
        this.disabled = new HashSet<>(disabled);
    }

    public boolean isEnabled(String ref) {
        return enabled.contains(ref);
    }

    public boolean isDisabled(String ref) {
        return disabled.contains(ref);
    }

    public Set<String> getEnabledSet() {
        return new HashSet<>(enabled);
    }

    public Set<String> getDisabledSet() {
        return new HashSet<>(disabled);
    }

    public void enable(Collection<String> items) {
        disabled.removeAll(items);
        enabled.addAll(items);
    }

    public void disable(Collection<String> items) {
        this.enabled.removeAll(items);
        this.disabled.addAll(items);
    }

}
