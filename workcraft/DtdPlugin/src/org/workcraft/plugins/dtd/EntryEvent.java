package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;

@DisplayName("Entry")
@IdentifierPrefix(value = "entry", isInternal = true)
@VisualClass(VisualEvent.class)
public class EntryEvent extends Event {

}
