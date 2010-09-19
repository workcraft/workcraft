package org.workcraft.plugins;

import org.workcraft.*;
import org.workcraft.plugins.desij.tools.Decomposition;
import org.workcraft.plugins.desij.tools.DesiJCustomFunction;
import org.workcraft.plugins.interop.CscResolutionTool;
import org.workcraft.plugins.layout.DotLayout;
import org.workcraft.plugins.layout.NullLayout;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.mpsat.MpsatSynthesis;
import org.workcraft.plugins.pcomp.gui.PcompTool;
import org.workcraft.plugins.petrify.tools.ComplexGateSynthesis;
import org.workcraft.plugins.petrify.tools.DummyContraction;
import org.workcraft.plugins.petrify.tools.ShowSg;
import org.workcraft.plugins.verification.tools.CustomPropertyMpsatChecker;
import org.workcraft.plugins.verification.tools.MpsatDeadlockChecker;

public class Tools implements Module {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]{
		};
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new ComplexGateSynthesis(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new CustomPropertyMpsatChecker(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new Decomposition(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new DesiJCustomFunction(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new DotLayout(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new MpsatDeadlockChecker(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new NullLayout(); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new PcompTool(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new RandomLayout(); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new ShowSg(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new CscResolutionTool(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new MpsatSynthesis(framework); } });
		p.registerClass(Tool.class, new Initialiser<Tool>() { public Tool create(){ return new DummyContraction(framework); } });
	}

}
