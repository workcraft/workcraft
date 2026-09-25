package org.workcraft.gui.properties;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class Toggle {

    private final LinkedHashSet<String> options;
    private final Consumer<String> consumer;
    private String selectedOption;

    public Toggle(LinkedHashSet<String> options, Consumer<String> consumer) {
        this(options, consumer, null);
    }

    public Toggle(LinkedHashSet<String> options, Consumer<String> consumer, String selectedOption) {
        this.options = options;
        this.consumer = consumer;
        apply(selectedOption);
    }

    public LinkedHashSet<String> getOptions() {
        return options;
    }

    public void apply(String option) {
        if (isValidOption(option)) {
            selectedOption = option;
            consumer.accept(option);
        }
    }

    public boolean isValidOption(String option) {
        return (option != null) && options.contains(option);
    }

    public boolean isSelectedOption(String option) {
        return (option != null) && option.equals(selectedOption);
    }


}
