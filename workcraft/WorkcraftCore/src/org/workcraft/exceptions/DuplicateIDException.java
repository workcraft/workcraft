package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class DuplicateIDException extends RuntimeException {

    private final int id;

    public DuplicateIDException(int id) {
        super(Integer.toString(id));
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
