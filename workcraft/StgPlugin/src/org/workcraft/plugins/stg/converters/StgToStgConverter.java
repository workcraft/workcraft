package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.petri.converters.DefaultPetriConverter;
import org.workcraft.plugins.stg.*;

import java.util.Map;

public class StgToStgConverter extends DefaultPetriConverter<VisualStg, VisualStg> {

    public StgToStgConverter(VisualStg srcModel) {
        this(srcModel, new VisualStg(new Stg()));
    }

    public StgToStgConverter(VisualStg srcModel, VisualStg dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(StgPlace.class, StgPlace.class);
        result.put(DummyTransition.class, DummyTransition.class);
        result.put(SignalTransition.class, SignalTransition.class);
        return result;
    }

    @Override
    public void copyStyle(Stylable srcStylable, Stylable dstStylable) {
        super.copyStyle(srcStylable, dstStylable);
        if ((srcStylable instanceof VisualSignalTransition srcTransition)
                && (dstStylable instanceof VisualSignalTransition dstTransition)) {

            Signal.Type type = srcTransition.getReferencedComponent().getSignalType();
            dstTransition.getReferencedComponent().setSignalType(type);
        }
    }

}
