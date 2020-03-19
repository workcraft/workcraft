package org.workcraft.exceptions;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class ModelValidationException extends Exception {

    private final LinkedList<String> errors = new LinkedList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public LinkedList<String> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        String r = "Model contains following errors:\n";
        for (String e: errors) {
            r += e + "\n";
        }
        return r;
    }

}
