package org.workcraft.plugins.wtg;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.plugins.dtd.Dtd;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.Transition;
import org.workcraft.serialisation.References;
import org.workcraft.util.Identifier;

@VisualClass(org.workcraft.plugins.wtg.VisualWtg.class)
public class Wtg extends Dtd {

    public Wtg() {
        this(null, (References) null);
    }

    public Wtg(Container root, References refs) {
        this(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof Transition) return Identifier.createInternal("e");
                if (node instanceof Signal) return "x";
                if (node instanceof State) return "s";
                if (node instanceof Waveform) return "w";
                return super.getPrefix(node);
            }
        });
    }

    public Wtg(Container root, ReferenceManager man) {
        super(root, man);
        new InitialStateSupervisor().attach(getRoot());
    }

    @Override
    public boolean keepUnusedSymbols() {
        return true;
    }

}
