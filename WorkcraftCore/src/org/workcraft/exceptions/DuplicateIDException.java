package org.workcraft.exceptions;

public class DuplicateIDException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int id;

    public DuplicateIDException(int id) {
        super(Integer.toString(id));
        this.id = id;

    }
    public int getId() {
        return id;
    }
}
