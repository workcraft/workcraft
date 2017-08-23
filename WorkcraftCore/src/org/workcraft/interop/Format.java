package org.workcraft.interop;
import java.util.UUID;

public interface Format {
    UUID getUuid();
    String getName();
    String getExtension();
    String getDescription();
}
