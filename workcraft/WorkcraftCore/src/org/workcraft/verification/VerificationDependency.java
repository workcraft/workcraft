package org.workcraft.verification;

import org.workcraft.utils.ModelHasher;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Another model (e.g. the environment of a circuit) that a verification result depends on. The model is identified
 * by the references of the circuit components that use it, which are empty for the environment. Its file may be
 * absent, which is a valid state of the dependency.
 *
 * @param file file of the model, if any
 * @param components references of the circuit components that use the model, in natural order
 */
public record VerificationDependency(File file, List<String> components) {

    private static final String SEPARATOR = ", ";

    public VerificationDependency(File file) {
        this(file, Collections.emptyList());
    }

    public VerificationDependency {
        components = (components == null) ? Collections.emptyList() : List.copyOf(components);
    }

    /**
     * Identity of the dependency, which is the comma-separated list of the component references.
     */
    public String getKey() {
        return String.join(SEPARATOR, components);
    }

    /**
     * Hash of the math layer of the model in the file, calculated in the same way as for the verified model.
     *
     * @return null if the hash cannot be obtained, as the file cannot be read
     */
    public String getHash() {
        return ModelHasher.getFileHash(file);
    }

}
