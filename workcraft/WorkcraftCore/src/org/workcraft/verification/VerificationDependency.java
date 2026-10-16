package org.workcraft.verification;

import org.workcraft.utils.ModelHasher;

import java.io.File;

/**
 * Another model (e.g. the environment of a circuit) that a verification result depends on. The model is identified
 * by the role it plays in the verification. Its file may be absent, which is a valid state of the dependency.
 *
 * @param name role of the model in the verification
 * @param file file of the model, if any
 */
public record VerificationDependency(String name, File file) {

    /**
     * Hash of the math layer of the model in the file, calculated in the same way as for the verified model.
     *
     * @return null if the hash cannot be obtained, as the file cannot be read
     */
    public String getHash() {
        return ModelHasher.getFileHash(file);
    }

}
