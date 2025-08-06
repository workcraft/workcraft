package org.workcraft.commands;

import org.workcraft.workspace.WorkspaceEntry;

public interface Command extends Entry {

    Category DEFAULT_CATEGORY = new Category("Tools", Integer.MAX_VALUE);

    enum Visibility { NEVER, APPLICABLE, ALWAYS, APPLICABLE_POPUP_ONLY }

    class Category {
        private final String name;
        private final int priority;

        public Category(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }
    }

    class Section implements Entry {
        private final String displayName;
        private final Position position;
        private final int priority;

        public Section(String displayName) {
            this(displayName, null, 0);
        }

        public Section(String displayName, Position position, int priority) {
            this.displayName = displayName;
            this.position = position;
            this.priority = priority;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Position getPosition() {
            return position;
        }
        @Override
        public int getPriority() {
            return priority;
        }

    }

    boolean isApplicableTo(WorkspaceEntry we);

    void run(WorkspaceEntry we);

    default Visibility getVisibility() {
        return Visibility.APPLICABLE;
    }

    default Category getCategory() {
        return DEFAULT_CATEGORY;
    }

    default Section getSection() {
        return null;
    }

}
