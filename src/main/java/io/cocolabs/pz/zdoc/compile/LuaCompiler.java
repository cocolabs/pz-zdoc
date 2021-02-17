/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc.compile;

import java.util.*;

import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.collections4.set.PredicatedSet;
import org.jetbrains.annotations.UnmodifiableView;

import io.cocolabs.pz.zdoc.doc.ZomboidJavaDoc;
import io.cocolabs.pz.zdoc.doc.ZomboidLuaDoc;
import io.cocolabs.pz.zdoc.element.lua.*;
import io.cocolabs.pz.zdoc.logger.Logger;
import io.cocolabs.pz.zdoc.element.IClass;
import io.cocolabs.pz.zdoc.element.IField;
import io.cocolabs.pz.zdoc.element.IMethod;
import io.cocolabs.pz.zdoc.element.IParameter;
import io.cocolabs.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.lua.*;

public class LuaCompiler implements ICompiler<ZomboidLuaDoc> {

	private static final Map<String, LuaClass> GLOBAL_CLASSES = new HashMap<>();
	private static final Map<String, LuaClass> CACHED_CLASSES = new HashMap<>();
	private static final Map<String, LuaClass> GLOBAL_TYPES = new HashMap<>();
	private static final Map<String, LuaType> CACHED_TYPES = new HashMap<>();

	private final @UnmodifiableView Set<ZomboidJavaDoc> javaDocs;

	public LuaCompiler(Set<ZomboidJavaDoc> javaDocs) {
		this.javaDocs = Collections.unmodifiableSet(javaDocs);
	}

	private static LuaType resolveLuaType(IClass iClass) throws CompilerException {

		List<LuaType> otherTypes = new ArrayList<>();
		for (IClass typeParam : iClass.getTypeParameters())
		{
			String typeName = "Unknown";
			if (typeParam != null)
			{
				String paramName = typeParam.getName();
				LuaType cachedType = CACHED_TYPES.get(paramName);
				if (cachedType == null)
				{
					LuaClass globalClass = GLOBAL_CLASSES.get(paramName);
					if (globalClass == null)
					{
						typeName = resolveClassName(paramName);
						registerGlobalType(typeParam, typeName);
					}
					else typeName = cacheType(typeParam, globalClass.getName()).getName();
				}
				else typeName = cachedType.getName();
			}
			otherTypes.add(new LuaType(typeName));
		}
		String luaType, className = iClass.getName();
		LuaType cachedType = CACHED_TYPES.get(className);
		if (cachedType == null)
		{
			LuaClass globalClass = GLOBAL_CLASSES.get(className);
			if (globalClass == null)
			{
				luaType = resolveClassName(className);
				registerGlobalType(iClass, luaType);
			}
			else luaType = cacheType(iClass, globalClass.getName()).getName();
		}
		else luaType = cachedType.getName();
		return new LuaType(luaType, otherTypes);
	}

	private static LuaType cacheType(IClass clazz, String type) {

		LuaType result = new LuaType(type);
		CACHED_TYPES.put(clazz.getName(), result);
		Logger.debug("Caching lua type (class: %s, type: %s)", clazz.getName(), type);
		return result;
	}

	private static void registerGlobalType(IClass clazz, String type) throws CompilerException {

		LuaCompiler.cacheType(clazz, type);
		LuaClass globalTypeLuaClass;

		Class<?> typeClass = ((JavaClass) clazz).getClazz();
		if (typeClass.isArray())
		{
			typeClass = typeClass.getComponentType();
			String typeName = typeClass.getTypeName();
			// don't register if already registered
			if (isRegisteredGlobal(typeName)) {
				return;
			}
			String className = resolveClassName(typeName).replaceAll("\\[]", "");
			String typeClassName = typeClass.getCanonicalName().replaceAll("\\[]", "");
			LuaClass luaClass = new LuaClass(className, typeClassName);
			type = luaClass.getName();
			globalTypeLuaClass = luaClass;
		}
		else globalTypeLuaClass = new LuaClass(type, typeClass.getCanonicalName());
		GLOBAL_TYPES.put(type, globalTypeLuaClass);
		Logger.debug("Registering global lua type (key: %s, value: %s", type, globalTypeLuaClass);
	}

	private static LuaClass resolveLuaClass(String name) throws CompilerException {

		LuaClass cachedClass = CACHED_CLASSES.get(name);
		if (cachedClass == null)
		{
			String parentType = name.replace('$', '.');
			LuaClass result = new LuaClass(resolveClassName(name), parentType);

			GLOBAL_CLASSES.put(result.getName(), result);
			Logger.debug("Caching global class (key: %s, value: %s)", result.getName(), result);

			CACHED_CLASSES.put(name, result);
			Logger.debug("Caching class (key: %s, value: %s)", name, result);

			CACHED_TYPES.put(name, new LuaType(result.getName()));
			Logger.debug("Caching lua type (class: %s, type: %s)", name, result);

			return result;
		}
		else return cachedClass;
	}

	private static String resolveClassName(String signature) throws CompilerException {

		Logger.debug("Resolving class name for signature %s", signature);
		String[] packages = signature.split("\\.");
		if (packages.length > 1)
		{
			char[] cName = packages[packages.length - 1].toCharArray();
			StringBuilder sb = new StringBuilder();
			for (char c : cName) {
				sb.append(c == '$' ? '.' : c);
			}
			String result = sb.toString();
			if (isRegisteredGlobal(result))
			{
				String globalClass = result;
				for (int i = packages.length - 2; i >= 0 && isRegisteredGlobal(result); i--) {
					result = packages[i] + '_' + result;
				}
				Logger.debug("Resolved class name as %s to avoid conflict with global class %s", result, globalClass);
				if (isRegisteredGlobal(result))
				{
					String msg = "Unexpected class name (%s) duplicate detected!";
					throw new CompilerException(String.format(msg, result));
				}
			}
			return result;
		}
		// class does not reside in a package
		else return signature;
	}

	private static boolean isRegisteredGlobal(String name) {
		return GLOBAL_CLASSES.containsKey(name);
	}

	public static @UnmodifiableView Set<LuaClass> getGlobalTypes() {

		Set<LuaClass> result = new HashSet<>();
		/*
		 * filter out types that are already defined as global classes,
		 * they have their own declaration in dedicated files
		 */
		for (Map.Entry<String, LuaClass> entry : GLOBAL_TYPES.entrySet())
		{
			if (!GLOBAL_CLASSES.containsKey(entry.getKey())) {
				result.add(entry.getValue());
			}
		}
		/* represents ? parameter type
		 * since EmmyLua does not have a good format for notating parameterized types
		 * this is the best way we can note an unknown parameter type
		 */
		result.add(new LuaClass("Unknown"));
		return Collections.unmodifiableSet(result);
	}

	public Set<ZomboidLuaDoc> compile() throws CompilerException {

		Logger.info("Start compiling lua classes...");
		Set<ZomboidLuaDoc> result = PredicatedSet.predicatedSet(
				new HashSet<>(), PredicateUtils.notNullPredicate()
		);
		for (ZomboidJavaDoc javaDoc : javaDocs)
		{
			LuaClass luaClass = resolveLuaClass(javaDoc.getName());
			Logger.debug("Compiling lua class %s...", luaClass.getName());

			List<LuaField> luaFields = new ArrayList<>();
			for (IField field : javaDoc.getFields())
			{
				LuaType fieldType = resolveLuaType(field.getType());
				luaFields.add(new LuaField(fieldType, field.getName(), field.getModifier(), field.getComment()));
				Logger.debug("Compiled field %s", field.getName());
			}
			Set<LuaMethod> luaMethods = new HashSet<>();
			for (IMethod method : javaDoc.getMethods())
			{
				List<LuaParameter> parameters = new ArrayList<>();
				for (IParameter param : method.getParams())
				{
					LuaType paramClass = resolveLuaType(param.getType());
					parameters.add(new LuaParameter(paramClass, param.getName()));
				}
				luaMethods.add(LuaMethod.Builder.create(method.getName())
						.withOwner(luaClass).withModifier(method.getModifier())
						.withReturnType(resolveLuaType(method.getReturnType()))
						.withParams(parameters).withVarArg(method.hasVarArg())
						.withComment(method.getComment()).build());

				Logger.debug("Compiled method %s", method.getName());
			}
			result.add(new ZomboidLuaDoc(luaClass, luaFields, luaMethods));
			Logger.detail("Compiled lua class %s with %d fields and %d methods",
					luaClass.getName(), luaFields.size(), luaMethods.size());
		}
		Logger.info("Finished compiling %d/%d lua classes", result.size(), javaDocs.size());
		return result;
	}
}
