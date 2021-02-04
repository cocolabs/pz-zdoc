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
package io.yooksi.pz.zdoc.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings("unused")
public class Logger {

	private static final String JVM_PROPERTY = "zdoc.logger";

	private static final org.apache.logging.log4j.Logger logger;
	private static final LoggerType TYPE;

	static
	{
		String loggerName = System.getProperty(JVM_PROPERTY);
		TYPE = loggerName != null && !loggerName.isEmpty() ?
				LoggerType.get(loggerName, LoggerType.INFO) : LoggerType.INFO;

		logger = LogManager.getLogger(TYPE.name);
		logger.debug("Created logger type: " + TYPE.name);
	}

	/* Make the constructor private to disable instantiation */
	private Logger() {
		throw new UnsupportedOperationException();
	}

	public static org.apache.logging.log4j.Logger get() {
		return logger;
	}

	/*
	 * Short-hand methods to print logs to console. For more methods
	 * use the static getter method to get a hold of a Logger instance.
	 */
	public static void info(String log) {
		logger.info(log);
	}

	public static void info(String log, Object... params) {
		logger.info(log, params);
	}

	public static void error(String log) {
		logger.error(log);
	}

	public static void error(String log, Object... args) {
		logger.printf(Level.ERROR, log, args);
	}

	public static void error(String log, Throwable t) {
		logger.error(log, t);
	}

	public static void warn(String log) {
		logger.warn(log);
	}

	public static void warn(String format, Object... params) {
		logger.printf(Level.WARN, format, params);
	}

	public static void debug(String log) {
		logger.debug(log);
	}

	public static void debug(String format, Object... args) {
		logger.debug(format, args);
	}

	public static void debug(String log, Throwable t) {
		logger.debug(log, t);
	}

	public static void printf(Level level, String format, Object... params) {
		logger.printf(level, format, params);
	}
}
