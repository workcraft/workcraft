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
package org.workcraft.plugins.balsa.stg;

import japa.parser.ASTHelper;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

public final class MyAstHelper
{
	public static void addMarkerAnnotation(MethodDeclaration method, String annotation) {
		addAnnotation(method, new MarkerAnnotationExpr(new NameExpr(annotation)));
	}

	public static void addAnnotation(MethodDeclaration method, AnnotationExpr annotation) {
		List<AnnotationExpr> annotations = method.getAnnotations();
		if(annotations == null)
			method.setAnnotations(annotations = new ArrayList<AnnotationExpr>(1));

		annotations.add(annotation);
	}

	public static void addStatement(MethodDeclaration method, Statement statement) {
		BlockStmt body = method.getBody();
		if(body == null)
			method.setBody(body = new BlockStmt());
		addStatement(body, statement);
	}

	public static void addStatement(ConstructorDeclaration ctor, Statement statement) {
		BlockStmt body = ctor.getBlock();
		if(body == null)
			ctor.setBlock(body = new BlockStmt());
		addStatement(body, statement);
	}

	private static void addStatement(BlockStmt body, Statement statement) {
		List<Statement> statements = body.getStmts();
		if(statements == null)
			body.setStmts(statements = new ArrayList<Statement>(1));
		statements.add(statement);
	}

	public static void addParameter(ConstructorDeclaration ctor, String typeName, String paramName) {
        List<Parameter> parameters = ctor.getParameters();
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
            ctor.setParameters(parameters);
        }

        parameters.add(ASTHelper.createParameter(new ClassOrInterfaceType(typeName), paramName));
	}

	public static ConstructorDeclaration addNewConstructor(ClassOrInterfaceDeclaration c) {
		ConstructorDeclaration ctor = new ConstructorDeclaration(ModifierSet.PUBLIC, c.getName());
		ctor.setBlock(new BlockStmt());
		ASTHelper.addMember(c, ctor);
		return ctor;
	}
}
