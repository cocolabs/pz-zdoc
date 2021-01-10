/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2021 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc.compile;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.collections4.list.PredicatedList;
import org.apache.commons.collections4.set.PredicatedSet;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.zdoc.doc.ZomboidAPIDoc;
import io.yooksi.pz.zdoc.doc.ZomboidJavaDoc;
import io.yooksi.pz.zdoc.doc.detail.DetailParsingException;
import io.yooksi.pz.zdoc.doc.detail.FieldDetail;
import io.yooksi.pz.zdoc.doc.detail.MethodDetail;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.logger.Logger;
import zombie.Lua.LuaManager;
import zombie.core.Core;

public class JavaCompiler implements ICompiler<ZomboidJavaDoc> {

	private final Set<Class<?>> exposedJavaClasses;

	public JavaCompiler() throws CompilerException {
		try {
			exposedJavaClasses = Collections.unmodifiableSet(getExposedJava());
		}
		catch (ReflectiveOperationException e) {
			throw new CompilerException("Error occurred while reading exposed java", e);
		}
	}

	static List<JavaField> compileJavaFields(Class<?> clazz, @Nullable ZomboidAPIDoc doc) throws DetailParsingException {

		List<JavaField> result = PredicatedList.predicatedList(
				new ArrayList<>(), PredicateUtils.notNullPredicate()
		);
		FieldDetail fieldDetail = doc != null ? new FieldDetail(doc) : null;
		for (Field field : clazz.getDeclaredFields())
		{
			int typeParamCount = field.getType().getTypeParameters().length;
			if (typeParamCount > 0)
			{
				/* if the field is a parameterized type we are not going to be able
				 * to determine the exact type due to runtime erasure, so try to
				 * use the field data from online API page if possible
				 */
				JavaClass jField = new JavaClass(field.getType());
				if (doc != null)
				{
					JavaField docField = fieldDetail.getEntry(field.getName());
					if (docField != null)
					{
						/* extra care has to be taken to ensure that we are dealing with exactly
						 * the same object since API documentation is often out of date
						 */
						if (docField.getType().equals(jField, true))
						{
							/* matching field was found, use field data pulled from API page
							 * with written type parameters instead of declared field
							 */
							result.add(docField);
							continue;
						}
					}
				}
				/* when no matching field or API page was found, construct new JavaField
				 * with same properties as declared field but make parameterized types null
				 */
				MemberModifier modifier = new MemberModifier(field.getModifiers());
				result.add(new JavaField(jField, field.getName(), modifier));
			}
			/* the field is not a parameterized type,
			 * use declared Field object to construct JavaField instance
			 */
			else result.add(new JavaField(field));
		}
		return result;
	}

	static Set<JavaMethod> compileJavaMethods(Class<?> clazz, @Nullable ZomboidAPIDoc doc) throws DetailParsingException {

		Set<JavaMethod> result = PredicatedSet.predicatedSet(
				new HashSet<>(), PredicateUtils.notNullPredicate()
		);
		MethodDetail methodDetail = doc != null ? new MethodDetail(doc) : null;
		for (Method method : clazz.getDeclaredMethods())
		{
			JavaMethod jMethod = new JavaMethod(method);
			if (doc != null)
			{
				JavaMethod docMethod = methodDetail.getEntry(method.getName());
				if (docMethod != null && docMethod.equals(jMethod, true))
				{
					result.add(docMethod);
					continue;
				}
			}
			result.add(jMethod);
		}
		return result;
	}

	/**
	 * Initialize {@link LuaManager} and return a set of exposed Java classes.
	 *
	 * @return a set of exposed Java classes.
	 *
	 * @throws RuntimeException if the private field ({@code LuaManager.Exposer#exposed})
	 * 		holding the set of exposed Java classes could not be found.
	 */
	@SuppressWarnings("unchecked")
	public static HashSet<Class<?>> getExposedJava() throws ReflectiveOperationException {

		Class<?> exposerClass = Arrays.stream(LuaManager.class.getDeclaredClasses())
				.filter(c -> c.getName().equals("zombie.Lua.LuaManager$Exposer"))
				.findFirst().orElseThrow(ClassNotFoundException::new);

		se.krka.kahlua.j2se.J2SEPlatform platform =
				new se.krka.kahlua.j2se.J2SEPlatform();

		Constructor<?> constructor = exposerClass.getDeclaredConstructor(
				se.krka.kahlua.converter.KahluaConverterManager.class,
				se.krka.kahlua.vm.Platform.class,
				se.krka.kahlua.vm.KahluaTable.class
		);
		constructor.setAccessible(true);
		Object exposer = constructor.newInstance(
				new se.krka.kahlua.converter.KahluaConverterManager(),
				platform, platform.newEnvironment()
		);
		Method exposeAll = MethodUtils.getMatchingMethod(exposerClass, "exposeAll");
		exposeAll.setAccessible(true);
		try {
			Core.bDebug = true;
			exposeAll.invoke(exposer);
		}
		catch (InvocationTargetException e) {
			// this is expected
		}
		return (HashSet<Class<?>>) FieldUtils.readDeclaredField(
				exposer, "exposed", true
		);
	}

	public Set<ZomboidJavaDoc> compile() {

		Set<ZomboidJavaDoc> result = new HashSet<>();
		for (Class<?> exposedClass : exposedJavaClasses)
		{
			String classPath = JavaClass.getPathForClass(exposedClass);
			if (!classPath.isEmpty())
			{
				@Nullable ZomboidAPIDoc document = null;
				try {
					Logger.debug(String.format("Getting API page for class \"%s\"", classPath));
					document = ZomboidAPIDoc.getPage(Paths.get(classPath));
					if (document == null) {
						Logger.warn(String.format("Unable to find API page for path %s", classPath));
					}
				} catch (IOException e)
				{
					String msg = "Error occurred while getting API page for path %s";
					Logger.error(String.format(msg, classPath), e);
				}
				JavaClass javaClass = new JavaClass(exposedClass);
				List<JavaField> javaFields;
				try {
					javaFields = compileJavaFields(exposedClass, document);
				}
				catch (DetailParsingException e)
				{
					String msg = "Error occurred while compiling java fields for document %s";
					Logger.error(String.format(msg, Objects.requireNonNull(document).getName()), e);
					continue;
				}
				Set<JavaMethod> javaMethods;
				try {
					javaMethods = compileJavaMethods(exposedClass, document);
				}
				catch (DetailParsingException e)
				{
					String msg = "Error occurred while compiling java methods for document %s";
					Logger.error(String.format(msg, Objects.requireNonNull(document).getName()), e);
					continue;
				}
				result.add(new ZomboidJavaDoc(javaClass, javaFields, javaMethods));
			}
			else Logger.error(String.format("Unable to find path for Java class \"%s\", " +
					"might be an internal class.", exposedClass.getName()));
		}
		return result;
	}
}
