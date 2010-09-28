/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.plugins.balsa.stg.codegenerator;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.visitor.GenericVisitorAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.parsers.breeze.BreezeDefinition;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.ParameterDeclaration;
import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.parsers.breeze.dom.ArrayedDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.ArrayedSyncPortDeclaration;
import org.workcraft.parsers.breeze.dom.FullDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.DataPortDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.PortVisitor;
import org.workcraft.parsers.breeze.dom.SyncPortDeclaration;
import org.workcraft.parsers.breeze.expressions.visitors.ToJavaAstConverter;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveFullDataPullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveFullDataPushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.plugins.balsa.stg.ComponentStgBuilder;
import org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder;
import org.workcraft.plugins.balsa.stg.HandshakeExtractor;
import org.workcraft.plugins.balsa.stg.MyAstHelper;
import org.workcraft.plugins.balsa.stg.MyDumpVisitor;
import org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter;
import org.workcraft.plugins.balsa.stg.codegenerator.VisitableDataClassesGenerator.VisitorPatternDeclarations;
import org.workcraft.util.FileUtils;

import pcollections.PVector;

public class CodeGenerator {
	public static void generateBaseClasses(File projectPath, String[] packageName, BalsaSystem balsa) throws IOException
	{
		Collection<? extends PrimitivePart> definitions = getBreezeDefinitions(balsa);

		File destinationFolder = appendPath(projectPath, packageName);
		StringBuilder pckgBuilder = new StringBuilder();
		for(String pathElement : packageName)
			pckgBuilder.append(pathElement+".");
		pckgBuilder.deleteCharAt(pckgBuilder.length()-1);
		String pckg = pckgBuilder.toString();

		Map<String, Map<String, Type>> componentParameterTypes = new HashMap<String, Map<String,Type>>();
		Map<String, Map<String, Type>> componentHandshakesTypes = new HashMap<String, Map<String,Type>>();

		for(PrimitivePart def : definitions)
		{
			PVector<ParameterDeclaration> parameters = def.getParameters();
			Map<String, Type> paramTypes = new HashMap<String, Type>();
			Map<String, Type> hsTypes = new HashMap<String, Type>();
			for(ParameterDeclaration parameter : parameters)
				paramTypes.put(parameter.getName(), getParameterType(parameter));
			componentParameterTypes.put(def.getName(), paramTypes);

			for(PortDeclaration port : def.getPorts())
				hsTypes.put(port.getName(), getPortType(port, false));

			componentHandshakesTypes.put(def.getName()+"Handshakes", hsTypes);
		}

		VisitorPatternDeclarations visitableParametersClasses = VisitableDataClassesGenerator.generate(componentParameterTypes, "BreezeComponent");
		VisitorPatternDeclarations visitableHandshakesClasses = VisitableDataClassesGenerator.generate(componentHandshakesTypes, "BreezeComponentHandshakes");

		for(PrimitivePart def : definitions)
		{
			generatePropertiesConstructor(def, visitableParametersClasses.dataClasses.get(def.getName()));
			generateStgInterfaceConstructor(def, false, visitableHandshakesClasses.dataClasses.get(def.getName()+"Handshakes"));
		}

		List<TypeDeclaration> declarations = new ArrayList<TypeDeclaration>();

		//declarations.addAll(visitableParametersClasses.getAllTypeDeclarations());
		//declarations.addAll(visitableHandshakesClasses.getAllTypeDeclarations());

		for(PrimitivePart def : definitions)
			declarations.add(generateStgBuilder(def));


		for(TypeDeclaration decl : declarations)
		{
			CompilationUnit cu = new CompilationUnit();
			cu.setPackage(new PackageDeclaration(new NameExpr(pckg)));
			ASTHelper.addTypeDeclaration(cu, decl);

			String source = cu.toString();
			FileUtils.writeAllText(new File(destinationFolder, decl.getName()+".java"), source);
		}
	}

	private static Collection<? extends PrimitivePart> getBreezeDefinitions(BalsaSystem balsa) throws IOException {
		ArrayList<PrimitivePart> list = new ArrayList<PrimitivePart>();
		for(PrimitivePart primitive : new BreezeLibrary(balsa).getPrimitives())
		{
			list.add(primitive);
		}
		return list;
	}

	String[] STUBS_PATH = new String[]{"org", "workcraft", "plugins", "balsa", "stg", "implementations_stubs"};

	public void generateStubs(File path, BalsaSystem balsa) throws IOException
	{
		File destinationFolder = appendPath(path, STUBS_PATH);
		String pckg = "org.workcraft.plugins.balsa.stg.implementations";
		String generatedPckg = "org.workcraft.plugins.balsa.stg.generated";

		PackageDeclaration pakageDecl = new PackageDeclaration(new NameExpr(pckg));

		Collection<? extends PrimitivePart> breezeParts = getBreezeDefinitions(balsa);

		CompilationUnit selectorCU = new CompilationUnit();
		selectorCU.setPackage(pakageDecl);
		ASTHelper.addTypeDeclaration(selectorCU, generateSelector(breezeParts));
		MyDumpVisitor dump = new MyDumpVisitor();
		selectorCU.accept(dump, null);
		String selectorText = dump.getSource();
		FileUtils.writeAllText(new File(destinationFolder, getSelectorClassName() + ".java"), selectorText);

		for(PrimitivePart p : breezeParts)
		{
			CompilationUnit cu = new CompilationUnit();
			ArrayList<ImportDeclaration> imports = new ArrayList<ImportDeclaration>(1);
			imports.add(new ImportDeclaration(new NameExpr(generatedPckg), false, true));
			imports.add(new ImportDeclaration(new NameExpr("org.workcraft.plugins.balsa.stg.ArrayPortUtils"), true, true));
			imports.add(new ImportDeclaration(new NameExpr("org.workcraft.plugins.balsa.stg.StgBuilderUtils"), true, true));

			cu.setImports(imports);
			cu.setPackage(pakageDecl);

			TypeDeclaration stub = generateStub(p);
			ASTHelper.addTypeDeclaration(cu, stub);

			String source = cu.toString();
			FileUtils.writeAllText(new File(destinationFolder, stub.getName()+".java"), source);
		}
	}

	private static File appendPath(File path, String[] toAppend) {
		File result = path;
		for(String s : toAppend)
			result = new File(result, s);
		return result;
	}

	private TypeDeclaration generateSelector(Collection<? extends BreezeDefinition> breezeDefs) {
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC, false, getSelectorClassName());

		ClassOrInterfaceType returnType = new ClassOrInterfaceType(ComponentStgBuilder.class.getCanonicalName() + "<" + DynamicComponent.class.getCanonicalName() + ">");
		List<Parameter> params = new ArrayList<Parameter>(1);
		String componentParamName = "componentName";
		NameExpr componentParamRef = new NameExpr(componentParamName);
		params.add(ASTHelper.createParameter(new ClassOrInterfaceType(String.class.getCanonicalName()), componentParamName));
		MethodDeclaration select = new MethodDeclaration(ModifierSet.STATIC | ModifierSet.PUBLIC, returnType, "create", params);
		ASTHelper.addMember(c, select);

		Statement elseStatement = new ThrowStmt(new ObjectCreationExpr(null, new ClassOrInterfaceType(NotSupportedException.class.getCanonicalName()), null));
		for(BreezeDefinition def : breezeDefs)
		{
			PrimitivePart p = (PrimitivePart)def;
			MethodCallExpr condition = new MethodCallExpr(componentParamRef, "equals");
			ASTHelper.addArgument(condition, new StringLiteralExpr(p.getName()));
			Statement thenStmt = new ReturnStmt(new ObjectCreationExpr(null, new ClassOrInterfaceType(getStubClassName(p)),null));
			elseStatement = new IfStmt(condition, thenStmt, elseStatement);
		}

		MyAstHelper.addStatement(select, elseStatement);

		return c;
	}

	private String getSelectorClassName() {
		return "StgBuilderSelector";
	}

	private TypeDeclaration generateStub(PrimitivePart p) {
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC, false, getStubClassName(p));

		List<ClassOrInterfaceType> extendsList = new ArrayList<ClassOrInterfaceType>(1);
		extendsList.add(new ClassOrInterfaceType(getStgBuilderClassName(p)));
		c.setExtends(extendsList);

		return c;
	}

	private String getStubClassName(PrimitivePart p) {
		return p.getName() + "StgBuilder";
	}

	private static void generatePropertiesConstructor(PrimitivePart p, ClassOrInterfaceDeclaration c)
	{
		ConstructorDeclaration ctor = MyAstHelper.addNewConstructor(c);
		String parametersArg = "parameters";
		MyAstHelper.addParameter(ctor, ParameterScope.class.getCanonicalName(), parametersArg);
		NameExpr parametersRef = new NameExpr(parametersArg);

		for(ParameterDeclaration param : p.getParameters())
		{
			String paramName = param.getName();
			Type paramType = getParameterType(param);

			MethodCallExpr call = new MethodCallExpr(parametersRef, "get");
			ASTHelper.addArgument(call, new StringLiteralExpr(paramName));
			Expression cast = new CastExpr(getBoxingType(paramType), call);
			AssignExpr assign = new AssignExpr(new NameExpr(paramName), cast , Operator.assign);
			MyAstHelper.addStatement(ctor, new ExpressionStmt(assign));
		}
	}

	private static TypeDeclaration generatePropertiesClass(PrimitivePart p) {
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC | ModifierSet.STATIC, false, getPropertiesClassName(p));

		for(ParameterDeclaration param : p.getParameters())
		{
			String paramName = param.getName();
			Type paramType = getParameterType(param);

			ASTHelper.addMember(c, ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC | ModifierSet.FINAL, paramType, paramName));
		}

		generatePropertiesConstructor(p, c);

		return c;
	}

	static class BoxingVisitor extends GenericVisitorAdapter<ClassOrInterfaceType, Object>
	{
		@Override
		public ClassOrInterfaceType visit(PrimitiveType n, Object arg) {
			return new ClassOrInterfaceType(getClass(n.getType()).getCanonicalName());
		}

		@Override
		public ClassOrInterfaceType visit(ClassOrInterfaceType n, Object arg) {
			return n;
		}

		private Class<?> getClass(Primitive type) {
			if(type == Primitive.Boolean)
				return Boolean.class;
			else if(type == Primitive.Int)
				return Integer.class;
			else
				throw new NotSupportedException();
		}
	}

	private static ClassOrInterfaceType getBoxingType(Type paramType) {
		ClassOrInterfaceType result = paramType.accept(new BoxingVisitor(), null);
		if(result == null)
			throw new NotSupportedException("The given type is not supported");
		return result;
	}

	private static Type getParameterType(ParameterDeclaration param) {
		Class<?> type = param.getType().getJavaType();

		if(type.isPrimitive())
			return new PrimitiveType(classToPrimitive(type));
		else
			return new ClassOrInterfaceType(type.getCanonicalName());
	}

	private static Primitive classToPrimitive(Class<?> type)
	{
		if(type == java.lang.Boolean.TYPE)
			return Primitive.Boolean;
		if(type == java.lang.Character.TYPE)
			return Primitive.Char;
		if(type == java.lang.Byte.TYPE)
			return Primitive.Byte;
		if(type == java.lang.Short.TYPE)
			return Primitive.Short;
		if(type == java.lang.Integer.TYPE)
			return Primitive.Int;
		if(type == java.lang.Long.TYPE)
			return Primitive.Long;
		if(type == java.lang.Float.TYPE)
			return Primitive.Float;
		if(type == java.lang.Double.TYPE)
			return Primitive.Double;
		if(type == java.lang.Void.TYPE)
			throw new NotSupportedException("Void primitive type is not supported");
		throw new NotSupportedException(type.getCanonicalName() + " is not a primitive type");
	}

	private static TypeDeclaration generateHandshakesClass(PrimitivePart p)
	{
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC | ModifierSet.STATIC, false, getHandshakesClassName(p));

		generateHandshakesClassConstructor(p, c);

		for(PortDeclaration port : p.getPorts())
		{
			ClassOrInterfaceType portType = new ClassOrInterfaceType(Handshake.class.getCanonicalName());
			if(port.isArrayed())
				portType = new ClassOrInterfaceType(java.util.List.class.getCanonicalName()+"<"+portType.getName()+">");
			ASTHelper.addMember(c, ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC | ModifierSet.FINAL, portType, port.getName()));
		}

		return c;
	}

	private static void generateHandshakesClassConstructor(PrimitivePart p, ClassOrInterfaceDeclaration c) {
		ConstructorDeclaration ctor = MyAstHelper.addNewConstructor(c);
		String propertiesParamName = "component";
		MyAstHelper.addParameter(ctor, getPropertiesClassName(p), propertiesParamName);
		String handshakesParamName = "handshakes";
		MyAstHelper.addParameter(ctor, getHandshakeMapClassName(), handshakesParamName);

		NameExpr handshakesRef = new NameExpr(handshakesParamName);
		NameExpr propertiesRef = new NameExpr(propertiesParamName);


		for(PortDeclaration port : p.getPorts())
		{
			String portName = port.getName();

			NameExpr extractor = new NameExpr(HandshakeExtractor.class.getCanonicalName());

			String interpretMethodName = "extract";

			MethodCallExpr extract = new MethodCallExpr(extractor, interpretMethodName);

			ASTHelper.addArgument(extract, handshakesRef);
			ASTHelper.addArgument(extract, new StringLiteralExpr(portName));
			if(port.isArrayed())
				ASTHelper.addArgument(extract, translateExpression(propertiesRef, port.count()));

			AssignExpr assign = new AssignExpr(new NameExpr(portName), extract, Operator.assign);
			MyAstHelper.addStatement(ctor, new ExpressionStmt(assign));
		}
	}

	private static TypeDeclaration generateStgInterfaceClass(PrimitivePart p, boolean environment) {
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC | ModifierSet.STATIC, false, getStgInterfaceClassName(p, environment));

		generateStgInterfaceConstructor(p, environment, c);

		for(PortDeclaration port : p.getPorts())
		{
			ClassOrInterfaceType portType = getPortType(port, environment);
			ASTHelper.addMember(c, ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC | ModifierSet.FINAL, portType, port.getName()));
		}

		return c;
	}

	private static ClassOrInterfaceType getPortType(PortDeclaration port, boolean environment) {
		ClassOrInterfaceType singlePortType = getSinglePortType(port, environment);
		if(port.isArrayed())
			return new ClassOrInterfaceType("java.util.List<"+singlePortType.getName()+">");
		else
			return singlePortType;
	}

	private static void generateStgInterfaceConstructor(PrimitivePart p, boolean environment, ClassOrInterfaceDeclaration c) {

		ConstructorDeclaration ctor = MyAstHelper.addNewConstructor(c);
		String componentArg = "component";
		MyAstHelper.addParameter(ctor, getPropertiesClassName(p), componentArg);
		String handshakesArg = "handshakes";
		MyAstHelper.addParameter(ctor, getStgMapClassName(), handshakesArg);

		NameExpr handshakesRef = new NameExpr(handshakesArg);
		NameExpr componentRef = new NameExpr(componentArg);

		for(PortDeclaration port : p.getPorts())
		{
			String portName = port.getName();
			ClassOrInterfaceType singlePortType = getSinglePortType(port, environment);

			NameExpr interpreter = new NameExpr(StgHandshakeInterpreter.class.getCanonicalName());

			String interpretMethodName;
			if(port.isArrayed())
				interpretMethodName = "array";
			else
				interpretMethodName = "get";

			MethodCallExpr interpret = new MethodCallExpr(interpreter, interpretMethodName);

			ASTHelper.addArgument(interpret, handshakesRef);
			ASTHelper.addArgument(interpret, new StringLiteralExpr(portName));
			if(port.isArrayed())
				ASTHelper.addArgument(interpret, translateExpression(componentRef, port.count()));
			ASTHelper.addArgument(interpret, new ClassExpr(singlePortType));

			AssignExpr assign = new AssignExpr(new NameExpr(portName), interpret, Operator.assign);
			MyAstHelper.addStatement(ctor, new ExpressionStmt(assign));
		}

	}

	private static Expression translateExpression(NameExpr scope, org.workcraft.parsers.breeze.expressions.Expression<Integer> count) {
		return count.accept(new ToJavaAstConverter(scope));
	}

	private static ClassOrInterfaceType getSinglePortType(PortDeclaration port, final boolean environment) {
		String type;
		Class<?> c;

		c = port.accept(new PortVisitor<Class<?>>()
				{
					@Override public Class<?> visit(ArrayedDataPortDeclaration port) {
						return dataClass(port.isActive(), port.isInput());
					}

					private Class<?> dataClass(boolean active, boolean input) {
						if(active ^ environment)
							if(input ^ environment)
								return ActivePullStg.class;
							else
								return ActivePushStg.class;
						else
							if(input ^ environment)
								return PassivePushStg.class;
							else
								return PassivePullStg.class;
					}

					@Override public Class<?> visit(ArrayedSyncPortDeclaration port) {
						return syncClass(port);
					}

					@Override public Class<?> visit(SyncPortDeclaration port) {
						return syncClass(port);
					}

					private Class<?> syncClass(PortDeclaration port) {
						if(port.isActive() ^ environment)
							return ActiveSync.class;
						else
							return PassiveSync.class;
					}

					@Override public Class<?> visit(DataPortDeclaration port) {
						return dataClass(port.isActive(), port.isInput());
					}

					@Override
					public Class<?> visit(FullDataPortDeclaration port) {
						return fullDataClass(port.isActive(), port.isInput());
					}

					private Class<?> fullDataClass(boolean active, boolean input) {
						if(active ^ environment)
							if(input ^ environment)
								return ActiveFullDataPullStg.class;
							else
								return ActiveFullDataPushStg.class;
						else
							if(input ^ environment)
								return PassiveFullDataPushStg.class;
							else
								return PassiveFullDataPullStg.class;
					}

				});

		type = c.getCanonicalName();

		return new ClassOrInterfaceType(type);
	}

	private static TypeDeclaration generateStgBuilder(PrimitivePart p) {
		ClassOrInterfaceDeclaration c = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC | ModifierSet.ABSTRACT, false, getStgBuilderClassName(p));

		List<ClassOrInterfaceType> extendsList = new ArrayList<ClassOrInterfaceType>(1);
		extendsList.add(new ClassOrInterfaceType(getBaseClassName(p)));
		c.setExtends(extendsList);

		ASTHelper.addMember(c, generatePropertiesClass(p));
		ASTHelper.addMember(c, generateStgInterfaceClass(p, true));
		ASTHelper.addMember(c, generateStgInterfaceClass(p, false));
		ASTHelper.addMember(c, generateHandshakesClass(p));

		ASTHelper.addMember(c, generatePropertiesMethod(p));
		ASTHelper.addMember(c, generateStgHandshakesMethod(p));
		ASTHelper.addMember(c, generateHandshakesMethod(p));

		return c;
	}
	private static String getBaseClassName(PrimitivePart p) {
		return GeneratedComponentStgBuilder.class.getCanonicalName() +
		"<"
		+ getStgBuilderClassName(p) + "." + getPropertiesClassName(p) +
		", "
		+ getStgBuilderClassName(p) + "." + getStgInterfaceClassName(p, false) +
		", "
		+ getStgBuilderClassName(p) + "." + getHandshakesClassName(p) + ">";
	}

	private static BodyDeclaration generatePropertiesMethod(PrimitivePart p) {
		MethodDeclaration m = new MethodDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC, new VoidType(), "makeProperties");
		MyAstHelper.addMarkerAnnotation(m, "Override");
		ClassOrInterfaceType returnClass = new ClassOrInterfaceType(getPropertiesClassName(p));
		m.setType(returnClass);

		String componentParameterName = "parameters";
		ASTHelper.addParameter(m, new Parameter(new ClassOrInterfaceType(ParameterScope.class.getCanonicalName()), new VariableDeclaratorId(componentParameterName)));
		ArrayList<Expression> arguments = new ArrayList<Expression>(1);
		arguments.add(new NameExpr(componentParameterName));
		ObjectCreationExpr construct = new ObjectCreationExpr(null, returnClass, arguments);

		MyAstHelper.addStatement(m, new ReturnStmt(construct));

		return m;
	}

	private static BodyDeclaration generateStgHandshakesMethod(PrimitivePart p) {
		MethodDeclaration m = new MethodDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC, new VoidType(), "makeHandshakesStg");
		MyAstHelper.addMarkerAnnotation(m, "Override");
		ClassOrInterfaceType returnClass = new ClassOrInterfaceType(getStgInterfaceClassName(p, false));
		m.setType(returnClass);

		String componentParameterName = "component";
		String handshakesParameterName = "handshakes";
		ASTHelper.addParameter(m, new Parameter(new ClassOrInterfaceType(getPropertiesClassName(p)), new VariableDeclaratorId("component")));
		ASTHelper.addParameter(m, new Parameter(new ClassOrInterfaceType(getStgMapClassName()), new VariableDeclaratorId("handshakes")));
		ArrayList<Expression> arguments = new ArrayList<Expression>(2);
		arguments.add(new NameExpr(componentParameterName));
		arguments.add(new NameExpr(handshakesParameterName));
		ObjectCreationExpr construct = new ObjectCreationExpr(null, returnClass, arguments);
		MyAstHelper.addStatement(m, new ReturnStmt(construct));
		return m;
	}
	private static BodyDeclaration generateHandshakesMethod(PrimitivePart p) {
		MethodDeclaration m = new MethodDeclaration(ModifierSet.FINAL | ModifierSet.PUBLIC, new VoidType(), "makeHandshakes");
		MyAstHelper.addMarkerAnnotation(m, "Override");
		ClassOrInterfaceType returnClass = new ClassOrInterfaceType(getHandshakesClassName(p));
		m.setType(returnClass);

		String componentParameterName = "component";
		String handshakesParameterName = "handshakes";
		ASTHelper.addParameter(m, new Parameter(new ClassOrInterfaceType(getPropertiesClassName(p)), new VariableDeclaratorId("component")));
		ASTHelper.addParameter(m, new Parameter(new ClassOrInterfaceType(getHandshakeMapClassName()), new VariableDeclaratorId("handshakes")));
		ArrayList<Expression> arguments = new ArrayList<Expression>(2);
		arguments.add(new NameExpr(componentParameterName));
		arguments.add(new NameExpr(handshakesParameterName));
		ObjectCreationExpr construct = new ObjectCreationExpr(null, returnClass, arguments);
		MyAstHelper.addStatement(m, new ReturnStmt(construct));
		return m;
	}
	private static String getHandshakeMapClassName() {
		return Map.class.getCanonicalName()+"<"+String.class.getCanonicalName()+","+Handshake.class.getCanonicalName()+">";
	}

	private static String getStgMapClassName() {
		return Map.class.getCanonicalName() + "<" + String.class.getCanonicalName() + ", " + StgInterface.class.getCanonicalName() +">";
	}

	private static String getStgInterfaceClassName(PrimitivePart p, boolean environment) {
		return p.getName() + "StgInterface" + (environment?"Env":"");
	}

	private static String getHandshakesClassName(PrimitivePart p) {
		return p.getName() + "Handshakes";
	}

	private static String getPropertiesClassName(PrimitivePart p) {
		return p.getName();
	}

	private static String getStgBuilderClassName(PrimitivePart p) {
		return p.getName() + "StgBuilderBase";
	}
}
