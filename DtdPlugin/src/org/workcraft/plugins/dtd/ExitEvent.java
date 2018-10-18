package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;

@DisplayName("Exit")
@IdentifierPrefix(value = "exit", isInternal = true)
@VisualClass(VisualExitEvent.class)
public class ExitEvent extends Event {

}
