/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
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
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.Nullable;

import groovy.lang.Closure;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.jvm.tasks.Jar;
import org.gradle.util.GUtil;

@NonNullApi
public class ZDocJar extends Jar {

	private static final String GAME_VERSION_PROPERTY = "gameVersion";

	private static final String TASK_GROUP = "build";
	private static final String[] DEPENDANT_TASKS = new String[]{ "jar" };
	private static final Object[] DEPENDENCY_TASKS = new String[]{ "readGameVersion" };

	private final Project project = this.getProject();

	public ZDocJar() {

		getArchiveFileName().set(project.provider(() -> {
			ExtraPropertiesExtension ext = project.getExtensions().getExtraProperties();
			if (ext.has(GAME_VERSION_PROPERTY))
			{
				String name = GUtil.elvis(getArchiveBaseName().getOrNull(), "");
				name = name + maybe(name, getArchiveAppendix().getOrNull());

				Object pGameVersion = Objects.requireNonNull(ext.get(GAME_VERSION_PROPERTY));
				name = name + maybe(name, pGameVersion.toString().trim());
				name = name + maybe(name, getArchiveClassifier().getOrNull());

				String extension = this.getArchiveExtension().getOrNull();
				return name + (GUtil.isTrue(extension) ? "." + extension : "");
			}
			return getArchiveFileName().get();
		}));
	}

	@Override
	public Task configure(Closure closure) {
		Task configure = super.configure(closure);
		/*
		 * additional configuration actions
		 */
		setGroup(TASK_GROUP);
		dependsOn(DEPENDENCY_TASKS);
		for (String taskName : DEPENDANT_TASKS)
		{
			getTaskByName(taskName).dependsOn(this);
		}
		return configure;
	}

	private static String maybe(@Nullable String prefix, @Nullable String value) {
		return GUtil.isTrue(value) ? GUtil.isTrue(prefix) ? "-".concat(value) : value : "";
	}

	/**
	 * Returns single {@code Task} with the given name
	 *
	 * @throws NoSuchElementException when the requested task was not found
	 */
	private Task getTaskByName(String name) {
		try {
			return project.getTasksByName(name, false).iterator().next();
		}
		catch (NoSuchElementException e) {
			throw new NoSuchElementException("Unable to find " + name + " gradle task");
		}
	}
}
