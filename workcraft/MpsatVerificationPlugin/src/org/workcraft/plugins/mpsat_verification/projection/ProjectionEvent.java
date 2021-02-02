package org.workcraft.plugins.mpsat_verification.projection;

public class ProjectionEvent {

    public enum Tag {
        INPUT("input"),
        OUTPUT("output"),
        INTERNAL("internal"),
        DUMMY("dummy"),
        VIOLATION("violation"),
        NONE(null);

        private final String name;

        Tag(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public final Tag tag;
    public final String ref;

    public ProjectionEvent(Tag tag, String ref) {
        this.tag = tag;
        this.ref = ref;
    }

}
