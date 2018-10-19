package org.workcraft.plugins.mpsat.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Enabledness {
    private final HashSet<String> enabled;
    private final HashSet<String> disabled;
    private final HashSet<String> unknown;

    public Enabledness(Collection<String> enabled, Collection<String> disabled, Collection<String> unknown) {
        this.enabled = new HashSet<>(enabled);
        this.disabled = new HashSet<>(disabled);
        this.unknown = new HashSet<>(unknown);
    }

    public boolean isEnabled(String ref) {
        return enabled.contains(ref);
    }

    public boolean isDisabled(String ref) {
        return disabled.contains(ref);
    }

    public boolean isUnknown(String ref) {
        return unknown.contains(ref);
    }

    public Set<String> getEnabledSet() {
        return new HashSet<>(enabled);
    }

    public Set<String> getDisabledSet() {
        return new HashSet<>(disabled);
    }

    public Set<String> getUnknownSet() {
        return new HashSet<>(unknown);
    }

    public void alter(Collection<String> enabled, Collection<String> disabled, Collection<String> unknown) {
        this.enabled.removeAll(disabled);
        this.enabled.removeAll(unknown);
        this.enabled.addAll(enabled);

        this.disabled.removeAll(enabled);
        this.disabled.removeAll(unknown);
        this.disabled.addAll(disabled);

        this.unknown.removeAll(enabled);
        this.unknown.removeAll(disabled);
        this.unknown.addAll(unknown);
    }

}
