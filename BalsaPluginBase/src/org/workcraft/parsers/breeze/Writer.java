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
package org.workcraft.parsers.breeze;

import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

import org.workcraft.parsers.breeze.dom.ArrayedDataPortDeclaration;
import org.workcraft.parsers.breeze.dom.ArrayedSyncPortDeclaration;
import org.workcraft.parsers.breeze.dom.BooleanPortDeclaration;
import org.workcraft.parsers.breeze.dom.BreezeDocument;
import org.workcraft.parsers.breeze.dom.BreezePart;
import org.workcraft.parsers.breeze.dom.ChannelDeclaration;
import org.workcraft.parsers.breeze.dom.DataPortDeclaration;
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.dom.PortVisitor;
import org.workcraft.parsers.breeze.dom.RawBreezePartReference;
import org.workcraft.parsers.breeze.dom.SyncPortDeclaration;
import org.workcraft.parsers.breeze.expressions.AddExpression;
import org.workcraft.parsers.breeze.expressions.CaseExpression;
import org.workcraft.parsers.breeze.expressions.Constant;
import org.workcraft.parsers.breeze.expressions.ConstantArrayType;
import org.workcraft.parsers.breeze.expressions.Expression;
import org.workcraft.parsers.breeze.expressions.ParameterReference;
import org.workcraft.parsers.breeze.expressions.StringConcatenateExpression;
import org.workcraft.parsers.breeze.expressions.ToStringExpression;
import org.workcraft.parsers.breeze.expressions.VariableArrayType;
import org.workcraft.parsers.breeze.expressions.visitors.Visitor;

public class Writer
{
	private final PrintStream out;

	public Writer(PrintStream out)
	{
		firstWritten.push(false);
		this.out = out;
	}

	public void print(BreezePart part)
	{
		begin();
		literal("breeze-part");
		quoted(part.getName());
		printPorts(part.getPorts());
		printChannels(part.getChannels());
		printParts(part.getParts());
		end();
	}

	private void printParts(List<RawBreezePartReference> parts) {
		begin();
		literal("components");
		for(RawBreezePartReference part : parts)
			print(part);
		end();
	}

	private void print(RawBreezePartReference part) {
		begin();
		literal("component");

		quoted(part.name());

		begin();
		for(String s : part.parameters())
			quoted(s);
		end();

		begin();
		for(List<Integer> con : part.connections())
		{
			boolean decorate = con.size() != 1;
			if(decorate)
				begin();
			for(Integer i : con)
				literal(i+"");
			if(decorate)
				end();
		}
		end();

		end();
	}

	private void printChannels(List<ChannelDeclaration> channels) {
		begin();
		literal("channels");
		for(ChannelDeclaration channel : channels)
			print(channel);
		end();
	}

	private void print(ChannelDeclaration channel) {
		begin();
		literal(channel.type == ChannelType.SYNC ? "sync" : channel.type == ChannelType.PULL ? "pull" : "push");
		if(channel.type != ChannelType.SYNC)
			literal(channel.width + "");
		end();
	}

	private void printPorts(List<PortDeclaration> ports) {
		begin();
		literal("ports");
		for(PortDeclaration port : ports)
			print(port);
		end();
	}

	private void print(Expression<?> count) {
		count.accept(new Visitor<Object>()
				{
					@Override
					public Object visit(AddExpression e) {
						begin(); literal("+"); for(Expression<Integer> arg : e.getArgs()) arg.accept(this); end();
						return null;
					}

					@Override
					public <T1, T2> Object visit(CaseExpression<T1, T2> e) {
						begin(); literal("case");
							e.getToCheck().accept(this);
							for(int i=0;i<e.getConditions().size();i++)
							{
								begin();
									begin();
										e.getConditions().get(i).accept(this);
									end();
									e.getValues().get(i).accept(this);
								end();
							}
							begin();
								literal("else");
								e.getElseValue().accept(this);
							end();
						end();
						return null;
					}

					@Override
					public <T> Object visit(Constant<T> e) {
						literal(e.getValue().toString());
						return null;
					}

					@Override
					public <T> Object visit(ParameterReference<T> e) {
						begin(); literal("param"); quoted(e.getParameterName()); end();
						return null;
					}

					@Override
					public Object visit(StringConcatenateExpression e) {
						begin();
						literal("string-append");
						for(Expression<String> i : e.getArgs())
							i.accept(this);
						end();
						return null;
					}

					@Override
					public <T> Object visit(ToStringExpression<T> e) {
						begin();
						literal("number->string");
						e.getArg().accept(this);
						end();
						return null;
					}

					@Override
					public Object visit(VariableArrayType e) {
						begin();
						literal("variable-array-type");
						e.getWidth().accept(this);
						literal("0");
						e.getReadPortCount().accept(this);
						e.getSpecification().accept(this);
						end();
						return null;
					}

					@Override
					public Object visit(ConstantArrayType constantArrayType) {
						constantArrayType.getWidth().accept(this);
						return null;
					}
				}
		);
	}

	private void print(PortDeclaration port) {
		begin();

		port.accept(new PortVisitor<Object>()
				{
					@Override public Object visit(ArrayedDataPortDeclaration port) {
						portHead(port, "arrayed-port");
						print(port.getCount());
						literal(port.isInput() ? "input" : "output");
						print(port.getWidth());
						return null;
					}

					@Override public Object visit(ArrayedSyncPortDeclaration port) {
						portHead(port, "arrayed-sync-port");
						print(port.getCount());
						return null;
					}

					@Override
					public Object visit(SyncPortDeclaration port) {
						portHead(port, "sync-port");
						return null;
					}

					@Override
					public Object visit(DataPortDeclaration port) {
						portHead(port, "port");
						literal(port.isInput() ? "input" : "output");
						print(port.getWidth());
						return null;
					}

					private void portHead(PortDeclaration port, String portType) {
						literal(portType);
						quoted(port.getName());
						literal(port.isActive() ? "active" : "passive");
					}

					@Override
					public Object visit(BooleanPortDeclaration port) {
						throw new org.workcraft.exceptions.NotImplementedException();
					}
				}
		);

		end();
	}

	private void quoted(String string) {
		writing();
		out.print("\"" + string + "\"");
	}

	private void literal(String string) {
		writing();
		out.print(string);
	}

	private void end() {
		if(firstWritten.pop())
		{
			out.println();
			indent();
		}
		out.print(")");
	}

	Stack<Boolean> firstWritten = new Stack<Boolean>();

	private void indent()
	{
		for(int i=0;i<firstWritten.size()-1;i++)
			out.print("\t");
	}

	private void writing()
	{
		if(firstWritten.peek())
		{
			out.println();
			indent();
		}
		else
		{
			firstWritten.pop();
			firstWritten.push(true);
		}
	}

	private void begin() {
		writing();
		out.print("(");
		firstWritten.push(false);
	}

	public void print(BreezeDocument breeze) {
		for(BreezePart p : breeze.getParts())
			print(p);
	}
}
