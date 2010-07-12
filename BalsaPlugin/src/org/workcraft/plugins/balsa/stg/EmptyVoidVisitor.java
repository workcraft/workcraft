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

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import japa.parser.ast.visitor.VoidVisitor;

public class EmptyVoidVisitor<T> implements VoidVisitor<T> {

	@Override
	public void visit(CompilationUnit n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(PackageDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ImportDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(TypeParameter n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(LineComment n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(BlockComment n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EnumDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EmptyTypeDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EnumConstantDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(AnnotationDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(AnnotationMemberDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(FieldDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(VariableDeclarator n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(VariableDeclaratorId n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ConstructorDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(MethodDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(Parameter n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EmptyMemberDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(InitializerDeclaration n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(JavadocComment n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ClassOrInterfaceType n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(PrimitiveType n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ReferenceType n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(VoidType n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(WildcardType n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ArrayAccessExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ArrayCreationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ArrayInitializerExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(AssignExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(BinaryExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(CastExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ClassExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ConditionalExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EnclosedExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(FieldAccessExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(InstanceOfExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(StringLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(IntegerLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(LongLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(IntegerLiteralMinValueExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(LongLiteralMinValueExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(CharLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(DoubleLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(BooleanLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(NullLiteralExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(MethodCallExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(NameExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ObjectCreationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(QualifiedNameExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ThisExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(SuperExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(UnaryExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(VariableDeclarationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(MarkerAnnotationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(NormalAnnotationExpr n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(MemberValuePair n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(TypeDeclarationStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(AssertStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(BlockStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(LabeledStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(EmptyStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ExpressionStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(SwitchStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(SwitchEntryStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(BreakStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ReturnStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(IfStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(WhileStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ContinueStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(DoStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ForeachStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ForStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(ThrowStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(SynchronizedStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(TryStmt n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

	@Override
	public void visit(CatchClause n, T arg) {
		throw new org.workcraft.exceptions.NotSupportedException();
	}

}
