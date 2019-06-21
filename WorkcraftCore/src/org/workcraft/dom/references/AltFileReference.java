package org.workcraft.dom.references;

import org.workcraft.utils.FileUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AltFileReference extends FileReference {

    private final Set<String> paths = new LinkedHashSet<>();

    @Override
    public void setBase(String base) {
        super.setBase(base);
        Set<String> tmpPaths = new LinkedHashSet<>(paths);
        clear();
        addAll(tmpPaths);
    }

    @Override
    public void setPath(String path) {
        super.setPath(path);
        add(path);
    }

    public boolean add(String path) {
        path = FileUtils.stripBase(path, getBase());
        if (path != null) {
            return paths.add(path);
        }
        return false;
    }

    public boolean addAll(Collection<String> paths) {
        boolean result = false;
        if (paths != null) {
            for (String path : paths) {
                result |= add(path);
            }
        }
        return result;
    }

    public boolean remove(String path) {
        path = FileUtils.stripBase(path, getBase());
        if (path != null) {
            if (path.equals(getPath())) {
                setPath(null);
            }
            return paths.remove(path);
        }
        return false;
    }

    public void clear() {
        setPath(null);
        paths.clear();
    }

    public Set<String> getPaths() {
        return Collections.unmodifiableSet(paths);
    }

}
