package org.workcraft.commands;

public abstract class AbstractVerificationCommand implements Command {

    public static final Category CATEGORY = new Category("Verification", 6);

    @Override
    public final Category getCategory() {
        return CATEGORY;
    }

}
