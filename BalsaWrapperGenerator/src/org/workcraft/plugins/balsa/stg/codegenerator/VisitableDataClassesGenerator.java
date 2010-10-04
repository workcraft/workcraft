package org.workcraft.plugins.balsa.stg.codegenerator;

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisitableDataClassesGenerator {

	static Set<String> reservedWords = new HashSet<String>(Arrays.asList(
		     new String[] {"while", "case", "continue"}
	));

	static class VisitorPatternDeclarations
	{
		public VisitorPatternDeclarations(
				Map<String, ClassOrInterfaceDeclaration> dataInterfaces,
				ClassOrInterfaceDeclaration visitorInterface,
				ClassOrInterfaceDeclaration visitableInterface,
				Map<String, ClassOrInterfaceDeclaration> dataClasses
				)
		{
			this.dataInterfaces = dataInterfaces;
			this.visitorInterface = visitorInterface;
			this.visitableInterface = visitableInterface;
			this.dataClasses = dataClasses;
		}

		public final Map<String, ClassOrInterfaceDeclaration> dataInterfaces;
		public final ClassOrInterfaceDeclaration visitorInterface;
		public final ClassOrInterfaceDeclaration visitableInterface;
		public final Map<String, ClassOrInterfaceDeclaration> dataClasses;

		public List<TypeDeclaration> getAllTypeDeclarations()
		{
			ArrayList<TypeDeclaration> result = new ArrayList<TypeDeclaration>();
			result.addAll(dataInterfaces.values());
			result.addAll(dataClasses.values());
			result.add(visitorInterface);
			result.add(visitableInterface);
			return result;
		}
	}

	public static VisitorPatternDeclarations generate(Map<String, Map<String, Type>> types, String commonName)
	{
		//1. data interfaces (getters)
		//2. visitor interface (needs data) (interface<T> with a lot of T visit(D))
		//3. visitable interface (needs visitor) (<T> T accept(visitor))
		//4. data classes (need everything)
		final Map<String, ClassOrInterfaceDeclaration> dataInterfaces = generateDataInterfaces(types);
		final ClassOrInterfaceDeclaration visitorInterface = generateVisitorInterface(types.keySet(), commonName);
		final ClassOrInterfaceDeclaration visitableInterface = generateVisitableInterface(commonName);
		final Map<String, ClassOrInterfaceDeclaration> dataClasses = generateDataClasses(types, commonName);
		return new VisitorPatternDeclarations(dataInterfaces, visitableInterface, visitorInterface, dataClasses);
	}

	private static Map<String, ClassOrInterfaceDeclaration> generateDataClasses(Map<String, Map<String, Type>> types, String commonName) {
		Map<String, ClassOrInterfaceDeclaration> result = new HashMap<String, ClassOrInterfaceDeclaration>();
		for(String name : types.keySet())
			result.put(name, generateDataClass(name, types.get(name), commonName));
		return result;
	}

	private static ClassOrInterfaceDeclaration generateDataClass(String name,
			Map<String, Type> members, String commonName) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, name+"Impl");
		result.setImplements(new ArrayList<ClassOrInterfaceType>());
		result.getImplements().add(new ClassOrInterfaceType(commonName));
		result.getImplements().add(new ClassOrInterfaceType(name));
		for(String memberName : members.keySet())
		{
			FieldDeclaration field = new FieldDeclaration(ModifierSet.PUBLIC, members.get(memberName), new VariableDeclarator(new VariableDeclaratorId(memberName)));
			ASTHelper.addMember(result, field);
			MethodDeclaration getter = new MethodDeclaration(ModifierSet.PUBLIC, members.get(memberName), getGetterName(memberName));
			getter.setBody(new BlockStmt());
			markOverride(getter);
			ASTHelper.addStmt(getter.getBody(), new ReturnStmt(new FieldAccessExpr(new ThisExpr(), memberName)));
			ASTHelper.addMember(result, getter);
		}
		MethodDeclaration accept = new MethodDeclaration(ModifierSet.PUBLIC, new VoidType(), "accept");
		markOverride(accept);
		String visitorParameterName = "visitor";
		ASTHelper.addParameter(accept, new Parameter(new ClassOrInterfaceType(getVisitorInterfaceName(commonName)), new VariableDeclaratorId(visitorParameterName)));
		accept.setBody(new BlockStmt());
		MethodCallExpr visitCall = new MethodCallExpr(new NameExpr(visitorParameterName), "visit");
		ASTHelper.addArgument(visitCall, new ThisExpr());
		ASTHelper.addStmt(accept.getBody(), new ExpressionStmt(visitCall));
		ASTHelper.addMember(result, accept);
		return result;
	}

	private static void markOverride(MethodDeclaration method) {
		AnnotationExpr annotation = new MarkerAnnotationExpr(new NameExpr("Override"));
		method.setAnnotations(Arrays.asList(new AnnotationExpr[]{annotation}));
	}

	private static String getGetterName(String memberName) {
		return "get"+Character.toUpperCase(memberName.charAt(0))+memberName.substring(1);
	}

	private static ClassOrInterfaceDeclaration generateVisitableInterface(String commonName) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, commonName);
		ClassOrInterfaceType visitorType = new ClassOrInterfaceType(getVisitorInterfaceName(commonName));
		MethodDeclaration accept = new MethodDeclaration(0, new VoidType(), "accept");
		ASTHelper.addParameter(accept, new Parameter(visitorType, new VariableDeclaratorId("visitor")));
		ASTHelper.addMember(result, accept);
		return result;
	}

	private static String getVisitorInterfaceName(String commonName) {
		return commonName+"Visitor";
	}

	private static ClassOrInterfaceDeclaration generateVisitorInterface(Set<String> visitableNames, String commonName) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, getVisitorInterfaceName(commonName));
		for(String name : visitableNames) {
			MethodDeclaration visit = new MethodDeclaration(0, new VoidType(), "visit");
			ASTHelper.addParameter(visit, new Parameter(new ClassOrInterfaceType(name), getVariableDecl(Character.toLowerCase(name.charAt(0))+name.substring(1))));
			ASTHelper.addMember(result, visit);
		}
		return result;
	}

	private static VariableDeclaratorId getVariableDecl(String name) {
		if(reservedWords.contains(name))
			name = name+"_";
		return new VariableDeclaratorId(name);
	}

	private static Map<String, ClassOrInterfaceDeclaration> generateDataInterfaces(Map<String, Map<String, Type>> types) {

		Map<String, ClassOrInterfaceDeclaration> result = new HashMap<String, ClassOrInterfaceDeclaration>();
		for(String name : types.keySet())
			result.put(name, generateDataInterface(name, types.get(name)));
		return result;
	}

	private static ClassOrInterfaceDeclaration generateDataInterface(String name, Map<String, Type> map) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, name);
		for(String memberName : map.keySet())
			ASTHelper.addMember(result, new MethodDeclaration(0, map.get(memberName), getGetterName(memberName)));
		return result;
	}
}
