package org.workcraft.plugins;

import org.workcraft.*;
import org.workcraft.plugins.desij.tools.Decomposition;
import org.workcraft.plugins.desij.tools.DesiJCustomFunction;
import org.workcraft.plugins.interop.CscResolutionTool;
import org.workcraft.plugins.layout.DotLayout;
import org.workcraft.plugins.layout.NullLayout;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.pcomp.gui.PcompTool;
import org.workcraft.plugins.petrify.tools.ComplexGateSynthesis;
import org.workcraft.plugins.petrify.tools.ShowSg;
import org.workcraft.plugins.verification.tools.CustomPropertyMpsatChecker;
import org.workcraft.plugins.verification.tools.MpsatDeadlockChecker;

public class Tools implements Plugin {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
		};
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ComplexGateSynthesis.class, new Initialiser() { public Object create(){ return new ComplexGateSynthesis(framework); } });
		p.registerClass(CustomPropertyMpsatChecker.class, new Initialiser() { public Object create(){ return new CustomPropertyMpsatChecker(framework); } });
		p.registerClass(Decomposition.class, new Initialiser() { public Object create(){ return new Decomposition(framework); } });
		p.registerClass(DesiJCustomFunction.class, new Initialiser() { public Object create(){ return new DesiJCustomFunction(framework); } });
		p.registerClass(DotLayout.class, new Initialiser() { public Object create(){ return new DotLayout(framework); } });
		p.registerClass(MpsatDeadlockChecker.class, new Initialiser() { public Object create(){ return new MpsatDeadlockChecker(framework); } });
		p.registerClass(NullLayout.class, new Initialiser() { public Object create(){ return new NullLayout(); } });
		p.registerClass(PcompTool.class, new Initialiser() { public Object create(){ return new PcompTool(framework); } });
		p.registerClass(RandomLayout.class, new Initialiser() { public Object create(){ return new RandomLayout(); } });
		p.registerClass(ShowSg.class, new Initialiser() { public Object create(){ return new ShowSg(framework); } });
		p.registerClass(CscResolutionTool.class, new Initialiser() { public Object create(){ return new CscResolutionTool(framework); } });
	}

}
