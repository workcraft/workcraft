package org.workcraft.plugins.balsa;

import org.workcraft.dom.VisualComponentGenerator;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;

public class BreezeVisualComponentGenerator implements VisualComponentGenerator {

	@Override
	public VisualNode createComponent(MathNode component, Object... constructorParameters){
		if(constructorParameters.length == 0)
			return new VisualBreezeComponent((BreezeComponent)component);
		/*if(constructorParameters.length == 1)
			if(constructorParameters[0] instanceof Element)
				try {
					return new VisualBreezeComponent((BreezeComponent)component, (Element)constructorParameters[0]);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}*/
		throw new RuntimeException("Unsupported constructor parameters!");
	}
}
