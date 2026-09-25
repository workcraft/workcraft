package org.workcraft.gui.properties;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class ToggleDeclaration extends PropertyDeclaration<Toggle> {

    public ToggleDeclaration(String name, LinkedHashSet<String> options, Consumer<String> consumer, String selectedOption) {
        this(name, new Toggle(options, consumer), selectedOption);
    }

    public ToggleDeclaration(String name, Toggle toggle, String defaultValue) {
        super(Toggle.class, name, value -> { }, () -> toggle);
        toggle.apply(defaultValue);
    }

}
