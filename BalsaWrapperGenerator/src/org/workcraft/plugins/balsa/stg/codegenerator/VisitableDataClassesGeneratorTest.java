package org.workcraft.plugins.balsa.stg.codegenerator;

import static org.junit.Assert.assertTrue;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.PrimitiveType.Primitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import org.junit.Test;
import org.workcraft.plugins.balsa.stg.codegenerator.VisitableDataClassesGenerator.VisitorPatternDeclarations;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class VisitableDataClassesGeneratorTest {

	private final class TestJavaFileManager implements
			JavaFileManager {
		private final ArrayList<TypeDeclaration> allDeclarations;
		private final StandardJavaFileManager standardFileManager;

		private TestJavaFileManager(
				ArrayList<TypeDeclaration> allDeclarations, StandardJavaFileManager standardFileManager) {
			this.allDeclarations = allDeclarations;
			this.standardFileManager = standardFileManager;
		}

		@Override
		public int isSupportedOption(String option) {
			if(true)throw new NotImplementedException();
			return -1;
		}

		@Override
		public ClassLoader getClassLoader(Location location) {
			return ClassLoader.getSystemClassLoader();
		}

		@Override
		public Iterable<JavaFileObject> list(Location location,
				String packageName, Set<Kind> kinds, boolean recurse)
				throws IOException {

			System.out.println("listing " + location.getName() + " package " + packageName);

			if(location.getName().equals("SOURCE_PATH"))
			{
				if(packageName.equals(""))
					return listRoot();
				else
					return Arrays.asList(new JavaFileObject[0]);
			}
			else
				return standardFileManager.list(location, packageName, kinds, recurse);
		}

		private Iterable<JavaFileObject> listRoot() {
			List<JavaFileObject> result = new ArrayList<JavaFileObject>();
			for(final TypeDeclaration decl : allDeclarations)
			{
				result.add(getJavaFileObject(decl));
			}
			return result;
		}

		private JavaFileObject getJavaFileObject(final TypeDeclaration decl) {

			JavaFileObject file = new JavaFileObject()
			{
				public Kind getKind() { return Kind.SOURCE; }
				public boolean delete() {return false;}
				public javax.lang.model.element.Modifier getAccessLevel() {return javax.lang.model.element.Modifier.PUBLIC; }
				public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException { return decl.toString(); }
				public long getLastModified() { return System.currentTimeMillis(); }
				public String getName() { return decl.getName()+".java"; }
				public javax.lang.model.element.NestingKind getNestingKind() {return null;}
				public boolean isNameCompatible(String simpleName, Kind kind) {return false;}
				public java.io.InputStream openInputStream() throws IOException { return new ByteArrayInputStream(decl.toString().getBytes()); }
				public java.io.OutputStream openOutputStream() throws IOException {throw new UnsupportedOperationException();}
				public java.io.Reader openReader(boolean ignoreEncodingErrors) throws IOException { return new StringReader(decl.toString()); }
				public java.io.Writer openWriter() throws IOException {throw new UnsupportedOperationException();}
				public java.net.URI toUri() {try {
					return new URI(decl.getName()+".java");
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException("fuck you");
				}}
				};
			return file;
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			if(!location.equals("SOURCE_PATH"))
			{
				String res = standardFileManager.inferBinaryName(location, file);
				if(res== null)
				{
					System.err.println("err: " + location + " : " + file);
				}
				return res;
			}
			else
			//System.out.println("   asked to inferBinaryName for location " + location.getName() + " and file " + file.getName());
			//System.out.println(" standardFileManager returned " + standardFileManager.inferBinaryName(location, file));
				return file.getName().replace(".java", "");//.replace(".java", ".class");
		}

		@Override
		public boolean isSameFile(FileObject a, FileObject b) {
			if(true)throw new NotImplementedException();
			return a.getName().equals(b.getName());
		}

		@Override
		public boolean handleOption(String current, Iterator<String> remaining) {
			if(true)throw new NotImplementedException();
			return false;
		}

		@Override
		public boolean hasLocation(Location location) {
			System.out.println("   asked about location " + location.getName());
			for(TypeDeclaration decl : allDeclarations)
				if(location.getName().contains(decl.getName()))
					return true;
			return true;
		}

		@Override
		public JavaFileObject getJavaFileForInput(Location location,
				String className, Kind kind) throws IOException {
			System.out.println("   requested java file from location " + location.getName() + ", class " + className);
			if(true)throw new NotImplementedException();
			for(TypeDeclaration decl : allDeclarations)
				if(location.getName().contains(decl.getName()))
					return getJavaFileObject(decl);
			return null;
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
				String className, Kind kind, FileObject sibling)
				throws IOException {
			if(true)throw new NotImplementedException();
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileObject getFileForInput(Location location,
				String packageName, String relativeName) throws IOException {
			if(true)throw new NotImplementedException();
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileObject getFileForOutput(Location location,
				String packageName, String relativeName, FileObject sibling)
				throws IOException {
			if(true)throw new NotImplementedException();
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
			if(true)throw new NotImplementedException();
			// TODO Auto-generated method stub

		}
	}

	@Test
	public void test1() throws Throwable
	{
		HashMap<String, Map<String, Type>> types = new HashMap<String, Map<String, Type>>();
		HashMap<String, Type> cowFields = new HashMap<String, Type>();
		cowFields.put("milkQuantity", new PrimitiveType(Primitive.Double));
		cowFields.put("name", new ClassOrInterfaceType("String"));
		types.put("Cow", cowFields);

		HashMap<String, Type> catFields = new HashMap<String, Type>();
		catFields.put("whiskersLength", new PrimitiveType(Primitive.Int));
		catFields.put("kittensNumber", new ClassOrInterfaceType("Integer"));
		types.put("Cat", catFields);
		VisitorPatternDeclarations result = VisitableDataClassesGenerator.generate(types, "Animal");

		final ArrayList<TypeDeclaration> allDeclarations = new ArrayList<TypeDeclaration>();
		allDeclarations.addAll(result.dataInterfaces.values());
		allDeclarations.add(result.visitorInterface);
		allDeclarations.add(result.visitableInterface);
		allDeclarations.addAll(result.dataClasses.values());

		StringBuilder b = new StringBuilder();
		for(TypeDeclaration decl : allDeclarations)
			b.append(decl.toString());

		System.out.println(b);

		StringWriter out = new StringWriter();

		DiagnosticListener<JavaFileObject> diagnostics = new DiagnosticListener<JavaFileObject>() {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				System.out.println(diagnostic.getMessage(null));
				System.out.println("from file: " + diagnostic.getSource().getName());
			}
		};

		JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);

		TestJavaFileManager fileManager = new TestJavaFileManager(allDeclarations, standardFileManager);

		//CompilationTask task = compiler.getTask(out, standardFileManager, diagnostics, null, null, fileManager.listRoot());

		//Boolean success = task.call();

		//System.out.println(out.toString());

		//assertTrue(success);
	}
}
